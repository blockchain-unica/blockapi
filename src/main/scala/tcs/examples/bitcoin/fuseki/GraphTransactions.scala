package tcs.examples.bitcoin.fuseki

import org.apache.jena.query.ResultSet
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet, Network}
import tcs.db.{DatabaseSettings, Fuseki}
import tcs.db.fuseki.{BlockchainURI, GraphModel}
import scala.collection.mutable
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer

class GraphTransactions(
                        val tx_hash: String = "",
                        val depth: Int = 4,
                        val network: Network = MainNet
                      ) {

  val fuseki = new DatabaseSettings("graphTransactions", Fuseki)

  val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", network))

  val model: GraphModel = new GraphModel(fuseki)

  var fst_model: Boolean = true

  var startBlock: Long = 1l
  var endBlock: Long = 300000l

  def startGraphTx(side: Direction = Both): Unit = {
    if (side.equals(Back))
      backTransactions()
    else if (side.equals(Forward))
      forwardTransactions()
    else if (side.equals(Both)) {
      backTransactions()
      forwardTransactions()
    }
  }

  def deleteGraphTx(): Unit = {
    model.deleteDataset()
  }

  def queryGraphTx(query: String): ResultSet = {
    model.datasetQuery(query)
  }

  private def backTransactions(): Unit = {

    var queue: mutable.Queue[(String, Int, String, Int)] = mutable.Queue()

    var tx = blockchain.getTransaction(tx_hash)
    if (fst_model) {
      println("Creation first graph: " + tx.hash.toString)
      model.addStatements(BlockchainURI.TX + tx.hash.toString,
        List(
          (BlockchainURI.TXHASH, tx.hash.toString),
          (BlockchainURI.TXSIZE, tx.txSize.toString),
          (BlockchainURI.LOCKTIME, tx.lock_time.toString),
          (BlockchainURI.DEPTH, "0")
        )
      )
      fst_model = false
    }

    tx.inputs.foreach(in => {

      var redeemedOutIndex : Int = -1
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
          (BlockchainURI.TX + tx.hash.toString, BlockchainURI.IN_PROP))

        redeemedOutIndex = in.redeemedOutIndex

      queue += ((in.redeemedTxHash.toString, 1, tx.hash.toString, redeemedOutIndex))
    })

    while (queue.nonEmpty) {
      val tl = queue.dequeue()
      if (tl._1 != "coinbase") {
        tx = blockchain.getTransaction(tl._1)

          tx.outputs.foreach(out => {
            if (tl._4 == out.index) {
              model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                List(
                  (BlockchainURI.INDEX, out.index.toString),
                  (BlockchainURI.VALUE, out.value.toString),
                  (BlockchainURI.OUTADDRESS, out.getAddress(network).get.toString)
                ),
                (BlockchainURI.IN + tl._3 + "/" + tl._1 + "/" + tl._4, BlockchainURI.BACKTX)
              )

              model.addStatements(BlockchainURI.TX + tx.hash.toString,
                List(
                  (BlockchainURI.TXHASH, tx.hash.toString),
                  (BlockchainURI.TXSIZE, tx.txSize.toString),
                  (BlockchainURI.LOCKTIME, tx.lock_time.toString),
                  (BlockchainURI.DEPTH, tl._2.toString)
                ),
                (BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString, BlockchainURI.ISOUTOF))
            }
          })



        if (tl._2 < depth) {
          tx.inputs.foreach(in => {

            var redeemedOutIndex : Int = -1

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
                (BlockchainURI.TX + tx.hash.toString, BlockchainURI.IN_PROP))

              redeemedOutIndex = in.redeemedOutIndex

            if (in.redeemedTxHash.toString != "0000000000000000000000000000000000000000000000000000000000000000") {
              queue += ((in.redeemedTxHash.toString, tl._2 + 1, tx.hash.toString, redeemedOutIndex))
            } else {
              queue += (("coinbase", tl._2 + 1, tx.hash.toString,redeemedOutIndex))

            }
          })
        }
      } else {
        model.addStatements(BlockchainURI.TX + "coinbase",
          List(
            (BlockchainURI.TXHASH, "coinbase"),
            (BlockchainURI.DEPTH, tl._2.toString)
          ),
          (BlockchainURI.TX + tl._3, BlockchainURI.BACKTX)
        )
      }
    }
    model.commit()
  }

  private def forwardTransactions(): Unit = {

    var transactionList: ArrayBuffer[(String, Int, List[Int])] = ArrayBuffer((tx_hash, 0, List()))

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
                  model.addStatements(BlockchainURI.TX + tx.hash.toString,
                    List(
                      (BlockchainURI.TXHASH, tx.hash.toString),
                      (BlockchainURI.TXSIZE, tx.txSize.toString),
                      (BlockchainURI.LOCKTIME, tx.lock_time.toString)
                    )
                  )
                  fst_model = false
                }

                var tmp_lst: List[Int] = List()

                tx.outputs.foreach(out => {
                  tmp_lst = out.index :: tmp_lst

                  model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                    List(
                      (BlockchainURI.INDEX, out.index.toString),
                      (BlockchainURI.VALUE, out.value.toString),
                      (BlockchainURI.OUTADDRESS, out.getAddress(network).get.toString)
                    ),
                    (BlockchainURI.TX + tx.hash.toString, BlockchainURI.OUT_PROP)
                  )
                })
                transactionList += ((tx_hash, 0, tmp_lst))
                fst = false
              }
            } else {
              tx.inputs.foreach(in => {

                // with Selection you can choose if the item into tmpChanges have to be added or removed from transactionList

                var tmpChanges: ArrayBuffer[(String, Int, List[Int], Selection)] = ArrayBuffer()

                transactionList.foreach(tl => {
                  if (in.redeemedTxHash.toString == tl._1) {
                    if (tl._3.contains(in.redeemedOutIndex)) {

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

                      model.addStatements(BlockchainURI.TX + tx.hash.toString,
                        List(
                          (BlockchainURI.TXHASH, tx.hash.toString),
                          (BlockchainURI.TXSIZE, tx.txSize.toString),
                          (BlockchainURI.LOCKTIME, tx.lock_time.toString)
                        ),
                        (BlockchainURI.IN + tx.hash.toString + "/" + in.redeemedTxHash.toString + "/" + in.redeemedOutIndex.toString, BlockchainURI.ISINOF)
                      )

                      var tmp_lst: List[Int] = List()
                      if (tl._2 < depth) {
                        tx.outputs.foreach(out => {
                          tmp_lst = out.index :: tmp_lst

                          model.addStatements(BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
                            List(
                              (BlockchainURI.INDEX, out.index.toString),
                              (BlockchainURI.VALUE, out.value.toString),
                              (BlockchainURI.OUTADDRESS, out.getAddress(network).get.toString)
                            ),
                            (BlockchainURI.TX + tx.hash.toString, BlockchainURI.OUT_PROP)
                          )
                        })
                      }

                      println("Forward tx: " + tx.hash.toString + " lv: " + (tl._2 + 1))

                      //item of the new transaction
                      tmpChanges += ((tx.hash.toString, tl._2 + 1, tmp_lst, Add))

                      val l = tl._3.filter(_ != in.redeemedOutIndex)

                      //item replacement
                      tmpChanges += ((tl._1, tl._2, tl._3, Delete)) //old item
                      tmpChanges += ((tl._1, tl._2, l, Add)) //new item
                    }
                  }

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

  def start(start : Long): GraphTransactions = {
    startBlock = start
    this
  }

  def end(end : Long) : GraphTransactions = {
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

