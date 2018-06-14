package it.unica.blockchain.analyses.bitcoin.fuseki

import org.apache.jena.query.ResultSet
import org.bitcoinj.core.Address
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin._
import it.unica.blockchain.db.{DatabaseSettings, Fuseki}
import it.unica.blockchain.db.fuseki.{BlockchainURI, GraphModel}

import scala.collection.mutable
import scala.util.control.Breaks._

/**
  * Creates an RDF view that represents a graph of addresses. If you decide to create a graph with "Back"
  * direction, the code starts from a transaction, makes a backward research of addresses that receive bitcoin from
  * other addresses, and store data into the database. Instead if you decide to create a graph with "Forward" direction,
  * the code starts form a transaction, makes a forward research of addresses that sent bitcoin to other addresses,
  * and store data into the database. You can also create both graphs with settings "Both".
  *
  * @param tx_hash Transaction hash
  * @param depth   Max depth of the graph
  * @param startBlock First block in the graph creation
  * @param endBlock Last block in the graph creation
  * @param network Either Bitcoin Main network or Bitcoin Test network.
  */

class AddressesGraph(
                      val tx_hash: String = "",
                      val depth: Int = 4,
                      val startBlock: Long = 1l,
                      val endBlock: Long = 300000l,
                      val network: Network = MainNet
                    ) {

  val fuseki = new DatabaseSettings("addresses", Fuseki)

  val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", network))

  val model: GraphModel = new GraphModel(fuseki, 500000l)

  var fst_model: Boolean = true

  def startAddressesGraph(side: Direction = Both): Unit = {
    if (side.equals(Back))
      backAddresses()
    else if (side.equals(Forward)) {
      forwardAddressesWithoutDepth()
    }
    else if (side.equals(Both)) {
      backAddresses()
      forwardAddressesWithoutDepth()
    }
  }

  /**
    * Delete the dataset
    */
  def delete(): Unit = {
    model.deleteDataset()
  }

  def queryGraphTx(query: String): ResultSet = {
    model.datasetQuery(query)
  }

  /**
    * Given a transaction, it stores the transaction into the model. Then load into a queue the transaction
    * hash and the output index informations contain in the input of the transaction. Next, the algorithm dequeue the
    * element from the queue, search the relative transaction with the function getTransaction(), and load the
    * transaction and their output addresses into the model. The algorithm runs until the queue is empty.
    */
  private def backAddresses(): Unit = {

    var queue: mutable.Queue[(String, Int, String, Int, Int)] = mutable.Queue()

    println("Start backAddresses...")
    var tx = blockchain.getTransaction(tx_hash)

    if (fst_model) {
      println("Creation first graph: " + tx.hash.toString)
      model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
        List(
          (BlockchainURI.TXHASH, tx.hash.toString),
          (BlockchainURI.TXSIZE, tx.txSize),
          (BlockchainURI.DATE, tx.date),
          (BlockchainURI.LOCKTIME, tx.lock_time)
        ),
        (BlockchainURI.TX + tx.hash.toString + "/-1", BlockchainURI.TX_PROP)
      )
      fst_model = false
    }

    tx.inputs.foreach(in => {
      queue += ((in.redeemedTxHash.toString, 1, tx.hash.toString, in.redeemedOutIndex, -1))
    })

    while (queue.nonEmpty) {
      val tl = queue.dequeue()
      if (tl._1 != "coinbase") {
        tx = blockchain.getTransaction(tl._1)

        var address: String = "unable_to_decode"

        val out: BitcoinOutput = tx.outputs.find(out => out.index == tl._4).orNull
        if (out != null) {
          address = out.getAddress(network) match {
            case Some(addr: Address) => addr.toString
            case None => "unable_to_decode"
          }

          println("Address: " + address + " Depth: " + tl._2)
          println("Tx1: " + tl._3)
          println("Tx2: " + tx.hash.toString)
          model.addStatements(BlockchainURI.ADDRESS + address,
            List(
              (BlockchainURI.ADDRESSPROP, address),
              (BlockchainURI.DEPTH, tl._2)
            ),
            (BlockchainURI.TX + tl._3 + "/" + tl._5.toString, BlockchainURI.BACKADDR)
          )

          if (address != "unable_to_decode") {
            model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
              List(
                (BlockchainURI.VALUE, out.value)
              ),
              (BlockchainURI.ADDRESS + address, BlockchainURI.RECEIVEDBY))

            model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
              List(
                (BlockchainURI.TXHASH, tx.hash.toString),
                (BlockchainURI.TXSIZE, tx.txSize),
                (BlockchainURI.DATE, tx.date),
                (BlockchainURI.LOCKTIME, tx.lock_time)
              ),
              (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.TX_PROP)
            )

            if (tl._2 < depth) {
              tx.inputs.foreach(in => {
                if (in.redeemedTxHash.toString != "0000000000000000000000000000000000000000000000000000000000000000") {
                  queue += ((in.redeemedTxHash.toString, tl._2 + 1, tx.hash.toString, in.redeemedOutIndex, out.index))
                }
              })
            }
          }
        }
      }
    }
    model.commit()
  }


  /**
    * Given a transaction, it stores the transaction and their output addresses data into the model, then stores the
    * transaction hash, the depth of the addresses relative to the graph, and the output references (output index and
    * relative address) into a list. Subsequently the algorithm search in each input, of each transaction, of each
    * forward block, if there's a match to transaction hash and output index contained into the input, and the
    * information inside the HashMap. If there's, the new transaction and their addresses data are loaded into the
    * model. The algorithm run as long as the list is empty (this happens when all addresses, with a depth less or equal
    * to max depth, have been finded), or if the final block has been reached.
    */

  private def forwardAddresses(): Unit = {

    var transactionMap: mutable.HashMap[String, (Set[Int], List[(Int, String)])] = mutable.HashMap()

    println("Start forwardTransaction..")

    var start: Long = startBlock

    var fst: Boolean = true
    var change: Boolean = true

    var startTime = System.currentTimeMillis() / 1000

    breakable {
      blockchain.start(start).end(endBlock).foreach(block => {
        change = true
        println(block.height)
        while (change) {
          change = false
          block.txs.foreach(tx => {
            if (fst) {
              if (tx.hash.toString == tx_hash) {
                println("Creation first graph: " + tx.hash.toString)
                model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
                  List(
                    (BlockchainURI.TXHASH, tx.hash.toString),
                    (BlockchainURI.TXSIZE, tx.txSize),
                    (BlockchainURI.TXDATE, tx.date),
                    (BlockchainURI.LOCKTIME, tx.lock_time)
                  )
                )

                var tmp_lst: List[(Int, String)] = List()
                tx.outputs.foreach(out => {

                  if (out.isOpreturn()) {
                    model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.INDEX, out.index.toString),
                        (BlockchainURI.ISOPRETURN, "true")
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.FORWARDADDR)
                    )
                  } else {
                    var address = out.getAddress(network) match {
                      case Some(addr: Address) => addr.toString
                      case None => "unable_to_decode"
                    }
                    println("address")

                    tmp_lst = (out.index, address) :: tmp_lst

                    model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.VALUE, out.value),
                        (BlockchainURI.TX_PROP, model.resource(BlockchainURI.TX_INFO + tx.hash.toString))
                      )
                    )

                    model.addStatements(BlockchainURI.ADDRESS + address,
                      List(
                        (BlockchainURI.ADDRESSPROP, address),
                        (BlockchainURI.DEPTH, 1)
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.FORWARDADDR)
                    )
                  }
                })
                transactionMap.put(tx_hash, (Set(2), tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {
                var tl = transactionMap.getOrElse(in.redeemedTxHash.toString, null)
                if (tl != null) {

                  val outInfo: (Int, String) = tl._2.find(t => t._1 == in.redeemedOutIndex).orNull

                  if (outInfo != null) {
                    println("-----" + tx.hash)
                    println(tl)
                    change = true

                    model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
                      List(
                        (BlockchainURI.TXHASH, tx.hash.toString),
                        (BlockchainURI.TXSIZE, tx.txSize),
                        (BlockchainURI.TXDATE, tx.date),
                        (BlockchainURI.LOCKTIME, tx.lock_time)
                      )
                    )
                    var tmp_lst: List[(Int, String)] = List()

                    var dp = tl._1.filter(p => p < depth)

                    if (dp.nonEmpty) {
                      tx.outputs.foreach(out => {
                        if (out.isOpreturn()) {

                          model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                            List(
                              (BlockchainURI.INDEX, out.index.toString),
                              (BlockchainURI.ISOPRETURN, "true")
                            ),
                            (BlockchainURI.TX + tx.hash.toString + "/1", BlockchainURI.FORWARDADDR)
                          )
                        } else {
                          var address = out.getAddress(network) match {
                            case Some(addr: Address) => addr.toString
                            case None => "unable_to_decode"
                          }
                          tmp_lst = (out.index, address) :: tmp_lst

                          var depth_prop = dp.map(d => (BlockchainURI.DEPTH, d))

                          model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                            List(
                              (BlockchainURI.VALUE, out.value),
                              (BlockchainURI.TX_PROP, model.resource(BlockchainURI.TX_INFO + tx.hash.toString))
                            ),
                            (BlockchainURI.ADDRESS + outInfo._2, BlockchainURI.SENTTO)
                          )

                          model.addStatements(BlockchainURI.ADDRESS + address,
                            List(
                              (BlockchainURI.ADDRESSPROP, address)
                            ) ++ depth_prop,
                            (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.FORWARDADDR)
                          )
                        }
                      })
                      //item of the new transaction
                      if (tmp_lst.nonEmpty) {
                        var tmp = transactionMap.getOrElse(tx.hash.toString, null)

                        if (tmp != null)
                          transactionMap.put(tx.hash.toString, (tmp._1 ++ dp.map(d => d + 1), tmp._2))
                        else
                          transactionMap.put(tx.hash.toString, (dp.map(d => d + 1), tmp_lst))
                      }
                    }

                    val l: List[(Int, String)] = tl._2.filter(_ != outInfo)

                    if (l.nonEmpty)
                      transactionMap.put(in.redeemedTxHash.toString, (tl._1, l))
                    else
                      transactionMap.remove(in.redeemedTxHash.toString)
                  }
                }
                if (transactionMap.isEmpty)
                  break
              })
            }
          })
        }
      })
    }
    model.commit()
    transactionMap.clear()

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
  }

  private def forwardAddressesWithoutDepth(): Unit = {
    var transactionMap: mutable.HashMap[String, List[(Int, String)]] = mutable.HashMap()

    println("Start forwardTransaction..")

    var start: Long = startBlock

    var fst: Boolean = true
    var change: Boolean = true

    var startTime = System.currentTimeMillis() / 1000

    breakable {
      blockchain.start(start).end(endBlock).foreach(block => {
        change = true
        println(block.height)
        while (change) {
          change = false
          block.txs.foreach(tx => {
            if (fst) {
              if (tx.hash.toString == tx_hash) {
                println("Creation first graph: " + tx.hash.toString)

                model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
                  List(
                    (BlockchainURI.TXHASH, tx.hash.toString),
                    (BlockchainURI.TXSIZE, tx.txSize),
                    (BlockchainURI.TXDATE, tx.date),
                    (BlockchainURI.LOCKTIME, tx.lock_time)
                  )
                )
                var tmp_lst: List[(Int, String)] = List()
                tx.outputs.foreach(out => {

                  if (out.isOpreturn()) {

                    model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.INDEX, out.index.toString),
                        (BlockchainURI.ISOPRETURN, "true")
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.FORWARDADDR)
                    )
                  } else {
                    var address = out.getAddress(network) match {
                      case Some(addr: Address) => addr.toString
                      case None => "unable_to_decode"
                    }
                    println("address")
                    tmp_lst = (out.index, address) :: tmp_lst

                    model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.VALUE, out.value),
                        (BlockchainURI.TX_PROP, model.resource(BlockchainURI.TX_INFO + tx.hash.toString))
                      )
                    )

                    model.addStatements(BlockchainURI.ADDRESS + address,
                      List(
                        (BlockchainURI.ADDRESSPROP, address),
                        (BlockchainURI.DEPTH, 1)
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.FORWARDADDR)
                    )
                  }
                })
                transactionMap.put(tx_hash, tmp_lst)
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {
                val tl = transactionMap.getOrElse(in.redeemedTxHash.toString, null)
                if (tl != null) {
                  val outInfo: (Int, String) = tl.find(t => t._1 == in.redeemedOutIndex).orNull
                  if (outInfo != null) {
                    println("-----" + tx.hash)
                    println(in.redeemedTxHash.toString + "-" + tl)
                    change = true


                    model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
                      List(
                        (BlockchainURI.TXHASH, tx.hash.toString),
                        (BlockchainURI.TXSIZE, tx.txSize),
                        (BlockchainURI.TXDATE, tx.date),
                        (BlockchainURI.LOCKTIME, tx.lock_time)
                      )
                    )
                    var tmp_lst: List[(Int, String)] = List()

                    tx.outputs.foreach(out => {
                      if (out.isOpreturn()) {


                        model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                          List(
                            (BlockchainURI.INDEX, out.index.toString),
                            (BlockchainURI.ISOPRETURN, "true")
                          ),
                          (BlockchainURI.TX + tx.hash.toString + "/1", BlockchainURI.FORWARDADDR)
                        )
                      } else {
                        var address = out.getAddress(network) match {
                          case Some(addr: Address) => addr.toString
                          case None => "unable_to_decode"
                        }
                        tmp_lst = (out.index, address) :: tmp_lst


                        model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                          List(
                            (BlockchainURI.VALUE, out.value),
                            (BlockchainURI.TX_PROP, model.resource(BlockchainURI.TX_INFO + tx.hash.toString))
                          ),
                          (BlockchainURI.ADDRESS + outInfo._2, BlockchainURI.SENTTO)
                        )


                        model.addStatements(BlockchainURI.ADDRESS + address,
                          List(
                            (BlockchainURI.ADDRESSPROP, address)
                          ),
                          (BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString, BlockchainURI.FORWARDADDR)
                        )
                      }
                    })

                    //item of the new transaction
                    if (tmp_lst.nonEmpty) {
                      transactionMap.put(tx.hash.toString, tmp_lst)
                    }

                    val l = tl.filter(_ != outInfo)

                    if (l.nonEmpty)
                      transactionMap.put(in.redeemedTxHash.toString, l)
                    else
                      transactionMap.remove(in.redeemedTxHash.toString)
                  }

                }
                if (transactionMap.isEmpty)
                  break
              })
            }
          })
        }
      })
    }
    model.commit()
    transactionMap.clear()

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
  }
}

