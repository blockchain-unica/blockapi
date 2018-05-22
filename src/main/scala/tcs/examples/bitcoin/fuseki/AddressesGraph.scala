package tcs.examples.bitcoin.fuseki

import org.apache.jena.query.ResultSet
import org.bitcoinj.core.Address
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin._
import tcs.db.{DatabaseSettings, Fuseki}
import tcs.db.fuseki.{BlockchainURI, GraphModel}

import scala.collection.mutable
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer

class AddressesGraph(
                      val tx_hash: String = "",
                      val depth: Int = 4,
                      val network: Network = MainNet
                    ) {

  val fuseki = new DatabaseSettings("addresses", Fuseki)

  val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", network))

  val model: GraphModel = new GraphModel(fuseki, 500000l)

  var fst_model: Boolean = true

  var startBlock: Long = 1l
  var endBlock: Long = 300000l

  def startAddressesGraph(side: Direction = Both): Unit = {
    if (side.equals(Back))
      backAddresses()
    else if (side.equals(Forward)) {
      forwardAddresses()
    }
    else if (side.equals(Both)) {
      backAddresses()
      forwardAddresses()
    }
  }

  def delete(): Unit = {
    model.deleteDataset()
  }

  def queryGraphTx(query: String): ResultSet = {
    model.datasetQuery(query)
  }

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

  private def forwardAddresses(): Unit = {

    var transactionList: ArrayBuffer[(String, Set[Int], List[(Int, String)])] = ArrayBuffer()

    println("Start forwardTransaction..")

    var start: Long = startBlock

    var fst: Boolean = true
    var change: Boolean = true

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
                if (fst_model) {
                  model.addStatements(BlockchainURI.TX_INFO + tx.hash.toString,
                    List(
                      (BlockchainURI.TXHASH, tx.hash.toString),
                      (BlockchainURI.TXSIZE, tx.txSize),
                      (BlockchainURI.TXDATE, tx.date),
                      (BlockchainURI.LOCKTIME, tx.lock_time)
                    ),
                    (BlockchainURI.TX + tx.hash.toString + "/-1", BlockchainURI.TX_PROP)
                  )
                  fst_model = false
                }

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

                    println("address")

                    tmp_lst = (out.index, address) :: tmp_lst

                    model.addStatements(BlockchainURI.ADDRESS + address,
                      List(
                        (BlockchainURI.ADDRESSPROP, address),
                        (BlockchainURI.DEPTH, 1)
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/-1", BlockchainURI.FORWARDADDR)
                    )

                    model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.VALUE, out.value)
                      ),
                      (BlockchainURI.ADDRESS + address, BlockchainURI.SENTTO)
                    )
                  }
                })
                transactionList += ((tx_hash, Set(2), tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {
                var tl = transactionList.find(tl => in.redeemedTxHash.toString == tl._1).orNull
                if (tl != null) {

                  val outInfo: (Int, String) = tl._3.find(t => t._1 == in.redeemedOutIndex).orNull

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
                      ),
                      (BlockchainURI.TX + tl._1 + "/" + outInfo._1, BlockchainURI.TX_PROP)
                    )
                    var tmp_lst: List[(Int, String)] = List()

                    var dp = tl._2.filter(p => p < depth)

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

                          model.addStatements(BlockchainURI.ADDRESS + address,
                            List(
                              (BlockchainURI.ADDRESSPROP, address)
                            ) ++ depth_prop,
                            (BlockchainURI.TX + tl._1 + "/" + outInfo._1, BlockchainURI.FORWARDADDR)
                          )
                          model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                            List(
                              (BlockchainURI.VALUE, out.value)
                            ),
                            (BlockchainURI.ADDRESS + address, BlockchainURI.SENTTO)
                          )
                        }
                      })
                      //item of the new transaction
                      if (tmp_lst.nonEmpty) {
                        val index = transactionList.indexWhere(p => p._1 == tx.hash.toString)

                        if (index != -1) {
                          val tmp = transactionList(index)
                          transactionList(index) = (tmp._1, tmp._2 ++ dp.map(d => d + 1), tmp._3)
                        }else{
                          transactionList += ((tx.hash.toString, dp.map(d => d + 1), tmp_lst))
                        }
                      }
                    }
                    val l = tl._3.filter(_ != outInfo)
                    //item replacement
                    transactionList -= ((tl._1, tl._2, tl._3)) //old item

                    if (l.nonEmpty)
                      transactionList += ((tl._1, tl._2, l)) //new item
                  }

                  //delete item when all outputs of the tx have been finded
                  if (tl._3.isEmpty)
                    transactionList -= ((tl._1, tl._2, tl._3))
                }
                if (transactionList.isEmpty)
                  break
              })
            }
          })
          //transactionList.foreach(tl => println(tl))
        }
      })
    }
    model.commit()
    transactionList.clear()
  }

  def start(start: Long): AddressesGraph = {
    startBlock = start
    this
  }

  def end(end: Long): AddressesGraph = {
    endBlock = end
    this
  }
}

