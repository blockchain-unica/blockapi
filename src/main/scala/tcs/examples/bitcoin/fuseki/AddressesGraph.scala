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

  private def backAddressesOld(): Unit = {

    var queue: mutable.Queue[(String, Int, String, Int)] = mutable.Queue()

    println("Start backAddresses...")
    var tx = blockchain.getTransaction(tx_hash)

    if (fst_model) {
      println("Creation first graph: " + tx.hash.toString)
      model.addStatements(BlockchainURI.TX + tx.hash.toString,
        List(
          (BlockchainURI.TXHASH, tx.hash.toString),
          (BlockchainURI.TXSIZE, tx.txSize),
          (BlockchainURI.DATE, tx.date),
          (BlockchainURI.LOCKTIME, tx.lock_time)
        )
      )
      fst_model = false
    }

    tx.inputs.foreach(in => {
      queue += ((in.redeemedTxHash.toString, 1, tx.hash.toString, in.redeemedOutIndex))
    })

    while (queue.nonEmpty) {
      val tl = queue.dequeue()
      if (tl._1 != "coinbase") {
        tx = blockchain.getTransaction(tl._1)

        var address: String = "unable_to_decode"

        val out: BitcoinOutput = tx.outputs.find(out => out.index == tl._4).orNull
        if (out != null) {
          address = out.getAddress(network) match {
            case Some(addr: Address) =>
              addr.toString
            case None => "unable_to_decode"
          }

          println("Address: " + address + "Depth: " + tl._2)
          println("Tx1: " + tl._3)
          println("Tx2: " + tx.hash.toString)
          model.addStatements(BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString,
            List(
              (BlockchainURI.ADDRESSPROP, address),
              (BlockchainURI.DEPTH, tl._2)
            ),
            (BlockchainURI.TX + tl._3, BlockchainURI.BACKADDR)
          )

          model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
            List(
              (BlockchainURI.INDEX, out.index),
              (BlockchainURI.VALUE, out.value),
              (BlockchainURI.OUTSCRIPT, out.outScript.toString)
            ),
            (BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString, BlockchainURI.OUTINFO)
          )

          if (address != "unable_to_decode") {
            model.addStatements(BlockchainURI.TX + tx.hash.toString,
              List(
                (BlockchainURI.TXHASH, tx.hash.toString),
                (BlockchainURI.TXSIZE, tx.txSize),
                (BlockchainURI.DATE, tx.date),
                (BlockchainURI.LOCKTIME, tx.lock_time)
              ),
              (BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString, BlockchainURI.ISOUTOF))
          }
        }

        if (tl._2 < depth && address != "unable_to_decode") {
          tx.inputs.foreach(in => {
            if (in.redeemedTxHash.toString != "0000000000000000000000000000000000000000000000000000000000000000") {
              queue += ((in.redeemedTxHash.toString, tl._2 + 1, tx.hash.toString, in.redeemedOutIndex))
            } /*else {
              queue += (("coinbase", tl._2 + 1, tx.hash.toString, in.redeemedOutIndex))
            }*/
          })
        }
      }
    }
    model.commit()
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

          println("Address: " + address + "Depth: " + tl._2)
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

  private def forwardAddressesOld(): Unit = {

    var transactionList: ArrayBuffer[(String, Int, List[(Int, String)])] = ArrayBuffer((tx_hash, 0, List()))

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
                  model.addStatements(BlockchainURI.TX + tx.hash.toString,
                    List(
                      (BlockchainURI.TXHASH, tx.hash.toString),
                      (BlockchainURI.TXSIZE, tx.txSize.toString),
                      (BlockchainURI.LOCKTIME, tx.lock_time.toString)
                    )
                  )
                  fst_model = false
                }

                var tmp_lst: List[(Int, String)] = List()
                tx.outputs.foreach(out => {

                  val address: String = out.getAddress(network) match {
                    case Some(addr: Address) =>
                      addr.toString
                    case None => ""
                  }

                  if (address != "") {
                    tmp_lst = (out.index, address) :: tmp_lst


                    model.addStatements(BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.ADDRESSPROP, address),
                        (BlockchainURI.DEPTH, 1.toString)
                      ),
                      (BlockchainURI.TX + tx.hash.toString, BlockchainURI.FORWARDADDR)
                    )

                    model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.INDEX, out.index.toString),
                        (BlockchainURI.VALUE, out.value.toString),
                        (BlockchainURI.OUTADDRESS, address)
                      ),
                      (BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString, BlockchainURI.OUTINFO)
                    )
                  }
                })
                transactionList += ((tx_hash, 2, tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {
                // with Selection you can choose if the item into tmpChanges have to be added or removed from transactionList

                var tmpChanges: ArrayBuffer[(String, Int, List[(Int, String)], Selection)] = ArrayBuffer()

                transactionList.foreach(tl => {
                  if (in.redeemedTxHash.toString == tl._1) {
                    val outInfo: (Int, String) = tl._3.find(t => {
                      t._1 == in.redeemedOutIndex
                    }).getOrElse((-1, ""))

                    if (outInfo != (-1, "")) {
                      change = true
                      model.addStatements(BlockchainURI.TX + tx.hash.toString,
                        List(
                          (BlockchainURI.TXHASH, tx.hash.toString),
                          (BlockchainURI.TXSIZE, tx.txSize.toString),
                          (BlockchainURI.LOCKTIME, tx.lock_time.toString)
                        ),
                        (BlockchainURI.ADDRESS + outInfo._2 + "/" + tl._1 + "/" + outInfo._1, BlockchainURI.ISINOF)
                      )

                      var tmp_lst: List[(Int, String)] = List()
                      if (tl._2 <= depth) {
                        tx.outputs.foreach(out => {

                          val address: String = out.getAddress(network) match {
                            case Some(addr: Address) =>
                              addr.toString
                            case None => ""
                          }

                          if (address != "") {
                            tmp_lst = (out.index, address) :: tmp_lst

                            model.addStatements(BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString,
                              List(
                                (BlockchainURI.ADDRESSPROP, address),
                                (BlockchainURI.DEPTH, tl._2.toString)
                              ),
                              (BlockchainURI.TX + tx.hash.toString, BlockchainURI.FORWARDADDR)
                            )

                            model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                              List(
                                (BlockchainURI.INDEX, out.index.toString),
                                (BlockchainURI.VALUE, out.value.toString),
                                (BlockchainURI.OUTADDRESS, address)
                              ),
                              (BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString, BlockchainURI.OUTINFO)
                            )
                          }
                        })
                      }

                      println("Forward tx: " + tx.hash.toString + " lv: " + (tl._2 + 1))

                      //item of the new transaction
                      tmpChanges += ((tx.hash.toString, tl._2 + 1, tmp_lst, Add))

                      val l = tl._3.filter(_ != outInfo)

                      //item replacement
                      tmpChanges += ((tl._1, tl._2, tl._3, Delete)) //old item
                      tmpChanges += ((tl._1, tl._2, l, Add)) //new item
                    }
                  }

                  //delete item when all outputs of the tx have been finded
                  if (tl._3.isEmpty)
                    tmpChanges += ((tl._1, tl._2, tl._3, Delete))
                })

                //transactionList update
                tmpChanges.foreach(tc => {
                  if (tc._4 == Add) {
                    transactionList += ((tc._1, tc._2, tc._3))
                  } else if (tc._4 == Delete) {
                    transactionList -= ((tc._1, tc._2, tc._3))
                  }
                })

                tmpChanges.clear()

                if (transactionList.isEmpty)
                  break
              })
            }
          })
          transactionList.foreach(tl => println(tl))
        }
      })
    }
    model.commit()
    transactionList.clear()
  }

  private def forwardAddressesOld2(): Unit = {

    var transactionList: ArrayBuffer[(String, Int, List[(Int, String)])] = ArrayBuffer()

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
                  model.addStatements(BlockchainURI.TX + tx.hash.toString,
                    List(
                      (BlockchainURI.TXHASH, tx.hash.toString),
                      (BlockchainURI.TXSIZE, tx.txSize),
                      (BlockchainURI.TXDATE, tx.date),
                      (BlockchainURI.LOCKTIME, tx.lock_time)
                    )
                  )
                  fst_model = false
                }

                var tmp_lst: List[(Int, String)] = List()
                tx.outputs.foreach(out => {

                  val address: String = out.getAddress(network) match {
                    case Some(addr: Address) =>
                      addr.toString
                    case None => "unable_to_decode"
                  }

                  if (address != "unable_to_decode") {
                    tmp_lst = (out.index, address) :: tmp_lst

                    model.addStatements(BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.ADDRESSPROP, address),
                        (BlockchainURI.DEPTH, 1)
                      ),
                      (BlockchainURI.TX + tx.hash.toString, BlockchainURI.FORWARDADDR)
                    )

                    model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.INDEX, out.index),
                        (BlockchainURI.VALUE, out.value),
                        (BlockchainURI.OUTADDRESS, address)
                      ),
                      (BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString, BlockchainURI.OUTINFO)
                    )
                  }
                })
                transactionList += ((tx_hash, 2, tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {

                var tl = transactionList.find(tl => in.redeemedTxHash.toString == tl._1).orNull

                if (tl != null) {

                  val outInfo: (Int, String) = tl._3.find(t => t._1 == in.redeemedOutIndex).orNull

                  if (outInfo != null) {
                    println(tl)
                    change = true
                    model.addStatements(BlockchainURI.TX + tx.hash.toString,
                      List(
                        (BlockchainURI.TXHASH, tx.hash.toString),
                        (BlockchainURI.TXSIZE, tx.txSize),
                        (BlockchainURI.TXDATE, tx.date),
                        (BlockchainURI.LOCKTIME, tx.lock_time)
                      ),
                      (BlockchainURI.ADDRESS + outInfo._2 + "/" + tl._1 + "/" + outInfo._1, BlockchainURI.ISINOF)
                    )

                    var tmp_lst: List[(Int, String)] = List()
                    if (tl._2 < depth) {
                      tx.outputs.foreach(out => {

                        val address: String = out.getAddress(network) match {
                          case Some(addr: Address) =>
                            addr.toString
                          case None => "unable_to_decode"
                        }

                        if (address != "unable_to_decode") {
                          tmp_lst = (out.index, address) :: tmp_lst
                        }

                        model.addStatements(BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString,
                          List(
                            (BlockchainURI.ADDRESSPROP, address),
                            (BlockchainURI.DEPTH, tl._2)
                          ),
                          (BlockchainURI.TX + tx.hash.toString, BlockchainURI.FORWARDADDR)
                        )

                        model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                          List(
                            (BlockchainURI.INDEX, out.index),
                            (BlockchainURI.VALUE, out.value),
                            (BlockchainURI.OUTADDRESS, address)
                          ),
                          (BlockchainURI.ADDRESS + address + "/" + tx.hash.toString + "/" + out.index.toString, BlockchainURI.OUTINFO)
                        )

                      })

                      //item of the new transaction
                      if (tmp_lst.nonEmpty)
                        transactionList += ((tx.hash.toString, tl._2 + 1, tmp_lst))
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
          transactionList.foreach(tl => println(tl))
        }
      })
    }
    model.commit()
    transactionList.clear()
  }

  private def forwardAddresses(): Unit = {

    var transactionList: ArrayBuffer[(String, Int, List[(Int, String)])] = ArrayBuffer()

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

                  val address: String = out.getAddress(network) match {
                    case Some(addr: Address) =>
                      addr.toString
                    case None => "unable_to_decode"
                  }

                  if (address != "unable_to_decode") {
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
                transactionList += ((tx_hash, 2, tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {

                var tl = transactionList.find(tl => in.redeemedTxHash.toString == tl._1).orNull

                if (tl != null) {

                  val outInfo: (Int, String) = tl._3.find(t => t._1 == in.redeemedOutIndex).orNull

                  if (outInfo != null) {
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
                    if (tl._2 < depth) {
                      tx.outputs.foreach(out => {

                        val address: String = out.getAddress(network) match {
                          case Some(addr: Address) =>
                            addr.toString
                          case None => "unable_to_decode"
                        }

                        if (address != "unable_to_decode") {
                          tmp_lst = (out.index, address) :: tmp_lst
                        }

                        model.addStatements(BlockchainURI.ADDRESS + address,
                          List(
                            (BlockchainURI.ADDRESSPROP, address),
                            (BlockchainURI.DEPTH, tl._2)
                          ),
                          (BlockchainURI.TX + tl._1 + "/" + outInfo._1, BlockchainURI.FORWARDADDR)
                        )

                        model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + out.index.toString,
                          List(
                            (BlockchainURI.VALUE, out.value)
                          ),
                          (BlockchainURI.ADDRESS + address, BlockchainURI.SENTTO)
                        )
                      })

                      //item of the new transaction
                      if (tmp_lst.nonEmpty)
                        transactionList += ((tx.hash.toString, tl._2 + 1, tmp_lst))
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
          transactionList.foreach(tl => println(tl))
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

