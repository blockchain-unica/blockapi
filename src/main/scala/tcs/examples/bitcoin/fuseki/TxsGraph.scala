package tcs.examples.bitcoin.fuseki

import org.apache.jena.query.ResultSet
import org.bitcoinj.core.Address
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet, Network}
import tcs.db.{DatabaseSettings, Fuseki}
import tcs.db.fuseki.{BlockchainURI, GraphModel}

import scala.collection.mutable
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer

class TxsGraph(
                val tx_hash: String = "",
                val depth: Int = 4,
                val network: Network = MainNet
              ) {

  val fuseki = new DatabaseSettings("txsGraph", Fuseki)

  val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", network))

  val model: GraphModel = new GraphModel(fuseki)

  var fst_model: Boolean = true

  var startBlock: Long = 1l
  var endBlock: Long = 300000l

  def startTxsGraph(side: Direction = Both): Unit = {
    if (side.equals(Back))
      backTransactions()
    else if (side.equals(Forward))
      forwardTransactions()
    else if (side.equals(Both)) {
      backTransactions()
      forwardTransactions()
    }
  }

  def deleteTxsGraph(): Unit = {
    model.deleteDataset()
  }

  def queryTxsGraph(query: String): ResultSet = {
    model.datasetQuery(query)
  }

  private def backTransactions(): Unit = {

    var queue: mutable.Queue[(String, Int, String, Int)] = mutable.Queue()

    var tx = blockchain.getTransaction(tx_hash)

    if (fst_model) {
      println("Creation first graph: " + tx.hash.toString)
      model.addStatements(BlockchainURI.TX + tx.hash.toString + "/1",
        List(
          (BlockchainURI.TXHASH, tx.hash.toString),
          (BlockchainURI.TXSIZE, tx.txSize.toString),
          (BlockchainURI.TXDATE, tx.date),
          (BlockchainURI.LOCKTIME, tx.lock_time.toString),
          (BlockchainURI.DEPTH, 1)
        )
      )
      fst_model = false
    }

    tx.inputs.foreach(in => {

      var redeemedOutIndex: Int = -1
      model.addStatements(BlockchainURI.IN + tx.hash.toString + "/" + in.redeemedTxHash.toString + "/" + in.redeemedOutIndex.toString,
        List(
          (BlockchainURI.REDEEMEDTXHASH, in.redeemedTxHash.toString),
          (BlockchainURI.INPUTVALUE, in.value.toString),
          (BlockchainURI.REDEEMEDOUTINDEX, in.redeemedOutIndex.toString),
          (BlockchainURI.ISCOINBASE, in.isCoinbase.toString),
          (BlockchainURI.SEQUENCENO, in.sequenceNo.toString),
          (BlockchainURI.OUTPOINT, in.outPoint.toString),
          (BlockchainURI.INSCRIPT, in.inScript.toString)
        ),
        (BlockchainURI.TX + tx.hash.toString + "/1", BlockchainURI.IN_PROP))

      redeemedOutIndex = in.redeemedOutIndex

      queue += ((in.redeemedTxHash.toString, 2, tx.hash.toString, redeemedOutIndex))
    })

    while (queue.nonEmpty) {
      val tl = queue.dequeue()
      println(tl)
      if (tl._1 != "coinbase") {
        tx = blockchain.getTransaction(tl._1)

        tx.outputs.foreach(out => {
          if (tl._4 == out.index) {

            val address: String = out.getAddress(network) match {
              case Some(addr: Address) =>
                addr.toString
              case None => ""
            }

            model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
              List(
                (BlockchainURI.INDEX, out.index.toString),
                (BlockchainURI.VALUE, out.value.toString),
                (BlockchainURI.OUTADDRESS, address)
              ),
              (BlockchainURI.IN + tl._3 + "/" + tl._1 + "/" + tl._4, BlockchainURI.BACKTX)
            )

            model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + tl._2.toString,
              List(
                (BlockchainURI.TXHASH, tx.hash.toString),
                (BlockchainURI.TXSIZE, tx.txSize.toString),
                (BlockchainURI.TXDATE, tx.date),
                (BlockchainURI.LOCKTIME, tx.lock_time.toString),
                (BlockchainURI.DEPTH, tl._2)
              ),
              (BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString, BlockchainURI.ISOUTOF))
          }
        })

        if (tl._2 < depth) {
          tx.inputs.foreach(in => {

            var redeemedTxHash: String = ""
            if (in.redeemedTxHash.toString == "0000000000000000000000000000000000000000000000000000000000000000") {
              redeemedTxHash = "coinbase"
            } else {
              redeemedTxHash = in.redeemedTxHash.toString
            }

            model.addStatements(BlockchainURI.IN + tx.hash.toString + "/" + redeemedTxHash + "/" + in.redeemedOutIndex.toString,
              List(
                (BlockchainURI.REDEEMEDTXHASH, redeemedTxHash),
                (BlockchainURI.INPUTVALUE, in.value.toString),
                (BlockchainURI.REDEEMEDOUTINDEX, in.redeemedOutIndex.toString),
                (BlockchainURI.ISCOINBASE, in.isCoinbase.toString),
                (BlockchainURI.SEQUENCENO, in.sequenceNo.toString),
                (BlockchainURI.OUTPOINT, in.outPoint.toString),
                (BlockchainURI.INSCRIPT, in.inScript.toString)
              ),
              (BlockchainURI.TX + tx.hash.toString + "/" + tl._2.toString, BlockchainURI.IN_PROP))

            queue += ((redeemedTxHash, tl._2 + 1, tx.hash.toString, in.redeemedOutIndex))
          })
        }
      } else {
        //println(tl)
        model.addStatements(BlockchainURI.TX + "coinbase" + "/" + tl._3 + "/" + tl._2.toString,
          List(
            (BlockchainURI.TXHASH, "coinbase"),
            (BlockchainURI.DEPTH, tl._2)
          ),
          (BlockchainURI.IN + tl._3 + "/" + tl._1 + "/" + tl._4, BlockchainURI.BACKTX)
        )
      }
    }
    model.commit()
  }

  private def forwardTransactions(): Unit = {

    var transactionList: ArrayBuffer[(String, Set[Int], List[Int])] = ArrayBuffer()

    println("Start forwardTransaction..")

    var start: Long = 0l

    start = startBlock

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
                  model.addStatements(BlockchainURI.TX + tx.hash.toString + "/1",
                    List(
                      (BlockchainURI.TXHASH, tx.hash.toString),
                      (BlockchainURI.TXSIZE, tx.txSize.toString),
                      (BlockchainURI.TXDATE, tx.date.toString),
                      (BlockchainURI.LOCKTIME, tx.lock_time.toString),
                      (BlockchainURI.DEPTH, 1)
                    )
                  )
                  fst_model = false
                }

                var tmp_lst: List[Int] = List()

                tx.outputs.foreach(out => {

                  if (out.isOpreturn()) {
                    model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.INDEX, out.index.toString),
                        (BlockchainURI.ISOPRETURN, "true")
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/1", BlockchainURI.OUT_PROP)
                    )
                  } else {
                    var address = out.getAddress(network) match {
                      case Some(addr: Address) => addr.toString
                      case None => "unable_to_decode"
                    }
                    model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                      List(
                        (BlockchainURI.INDEX, out.index.toString),
                        (BlockchainURI.VALUE, out.value.toString),
                        (BlockchainURI.OUTADDRESS, address)
                      ),
                      (BlockchainURI.TX + tx.hash.toString + "/1", BlockchainURI.OUT_PROP)
                    )

                    tmp_lst = out.index :: tmp_lst
                  }
                })
                transactionList += ((tx_hash, Set(2), tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {
                val tl : (String, Set[Int], List[Int]) = transactionList.find(tl => in.redeemedTxHash.toString == tl._1).orNull

                if (tl != null) {
                  if (tl._3.contains(in.redeemedOutIndex)) {

                    println(tl)
                    change = true

                    model.addStatements(BlockchainURI.IN + tx.hash.toString + "/" + in.redeemedTxHash.toString + "/" + in.redeemedOutIndex.toString,
                      List(
                        (BlockchainURI.REDEEMEDTXHASH, in.redeemedTxHash.toString),
                        (BlockchainURI.INPUTVALUE, in.value.toString),
                        (BlockchainURI.REDEEMEDOUTINDEX, in.redeemedOutIndex.toString),
                        (BlockchainURI.ISCOINBASE, in.isCoinbase.toString),
                        (BlockchainURI.SEQUENCENO, in.sequenceNo.toString),
                        (BlockchainURI.OUTPOINT, in.outPoint.toString),
                        (BlockchainURI.INSCRIPT, in.inScript.toString)
                      ),
                      (BlockchainURI.OUT + tl._1 + "/" + in.redeemedOutIndex, BlockchainURI.FORWARDTX)
                    )

                    val depth_prop = tl._2.map(d => (BlockchainURI.DEPTH, d))

                    model.addStatements(BlockchainURI.TX + tx.hash.toString + "/" + tl._2.toString,
                      List(
                        (BlockchainURI.TXHASH, tx.hash.toString),
                        (BlockchainURI.TXSIZE, tx.txSize.toString),
                        (BlockchainURI.TXDATE, tx.date.toString),
                        (BlockchainURI.LOCKTIME, tx.lock_time.toString)
                      ) ++ depth_prop,
                      (BlockchainURI.IN + tx.hash.toString + "/" + in.redeemedTxHash.toString + "/" + in.redeemedOutIndex.toString, BlockchainURI.ISINOF)
                    )

                    var tmp_lst: List[Int] = List()

                    var dp = tl._2.filter(p => p < depth)

                    if (dp.nonEmpty) {
                      tx.outputs.foreach(out => {

                        if (out.isOpreturn()) {
                          model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                            List(
                              (BlockchainURI.INDEX, out.index.toString),
                              (BlockchainURI.ISOPRETURN, "true")
                            ),
                            (BlockchainURI.TX + tx.hash.toString + "/" + tl._2.toString, BlockchainURI.OUT_PROP)
                          )
                        } else {
                          var address = out.getAddress(network) match {
                            case Some(addr: Address) => addr.toString
                            case None => "unable_to_decode"
                          }
                          model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                            List(
                              (BlockchainURI.INDEX, out.index.toString),
                              (BlockchainURI.VALUE, out.value.toString),
                              (BlockchainURI.OUTADDRESS, address)
                            ),
                            (BlockchainURI.TX + tx.hash.toString + "/" + tl._2.toString, BlockchainURI.OUT_PROP)
                          )

                          tmp_lst = out.index :: tmp_lst
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

                    val l = tl._3.filter(_ != in.redeemedOutIndex)

                    //item replacement
                    transactionList -= ((tl._1, tl._2, tl._3)) //old item

                    if (l.nonEmpty)
                      transactionList += ((tl._1, tl._2, l)) //new item
                  }

                  if (tl._3.isEmpty)
                    transactionList -= ((tl._1, tl._2, tl._3))

                  if (transactionList.isEmpty)
                    break
                }
              })
            }
          })
        }
      })
    }
    model.commit()
    transactionList.clear()
  }
  
  def start(start: Long): TxsGraph = {
    startBlock = start
    this
  }

  def end(end: Long): TxsGraph = {
    endBlock = end
    this
  }
}

class Direction

object Back extends Direction

object Forward extends Direction

object Both extends Direction

class Selection

object Add extends Selection

object Delete extends Selection

