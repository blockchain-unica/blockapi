package it.unica.blockchain.analyses.bitcoin.fuseki

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.fuseki.{BlockchainURI, GraphModel}
import it.unica.blockchain.db.{DatabaseSettings, Fuseki}
import it.unica.blockchain.externaldata.metadata.MetadataParser
import org.bitcoinj.core.ScriptException

import scala.collection.mutable

class OpReturnGraph(
                     val startBlock: Long = 1l,
                     val endBlock: Long = 400000l
                   ) {

  val fuseki = new DatabaseSettings("opReturn2", Fuseki)

  val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))

  val model: GraphModel = new GraphModel(fuseki, 50000l)

  def opReturnResearch() = {
    val transactionMap: mutable.HashMap[String, mutable.HashSet[Int]] = mutable.HashMap()

    blockchain.start(startBlock).end(endBlock).foreach(block => {
      if(block.height % 1000 == 0)
        println(block.height)

      block.txs.foreach(tx => {

        var outIndexList: mutable.HashSet[Int] = mutable.HashSet()
        var opReturn: Boolean = false
        var inputTxsList: List[String] = List()

        tx.inputs.foreach(in => {
          val outputList = transactionMap.getOrElse(in.redeemedTxHash.toString, null)

          if (outputList != null) {
            if (outputList.contains(in.redeemedOutIndex)) {

              //update or remove item of the input transaction from transactionMap
              outputList.remove(in.redeemedOutIndex)

              if (outputList.isEmpty)
                transactionMap.remove(in.redeemedTxHash.toString)
              else
                transactionMap.put(in.redeemedTxHash.toString, outputList)


              inputTxsList = in.redeemedTxHash.toString :: inputTxsList
            }
          }
        })

        tx.outputs.foreach(out => {
          //println(tx.hash + "/" + out.index)
          var isOpreturn: Boolean = false
          var exc : Boolean = false

          try {
            isOpreturn = out.isOpreturn()
          } catch {
            case sexp: ScriptException => {
              println("ScriptException - "+ block.height + " tx: " + tx.hash)
              isOpreturn = true
              exc = true
            }
            case iexp: IllegalStateException => {
              println("IllegalStateException - " + block.height + " tx: " + tx.hash)
              isOpreturn = true
              exc = true
            } //block: 310272 - tx: ce7d73cba662af3dfbd699384111115f4ccd24b3748a88bf9bc70c1cc4d08660
          }

          if (isOpreturn) {
            opReturn = true
            var protocol = ""
            var metadata = ""

            if(!exc) {
              protocol = MetadataParser.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.transOut.toString)
              metadata = out.getMetadata()
            }

            println(protocol)
            println(metadata)

            model.addStatements(BlockchainURI.OPRETURN + tx.hash,
              List(
                (BlockchainURI.PROTOCOL, protocol),
                (BlockchainURI.METADATA, metadata)
              ),
              (BlockchainURI.TX + tx.hash, BlockchainURI.OPRETURN_PROP)
            )
          } else {
            outIndexList.add(out.index)
          }
        })

        if (opReturn) {
          println("opReturn tx: " + tx.hash.toString)
          model.addStatements(BlockchainURI.TX + tx.hash.toString,
            List(
              (BlockchainURI.TXHASH, tx.hash.toString),
              (BlockchainURI.TXSIZE, tx.txSize.toString),
              (BlockchainURI.TXDATE, tx.date.toString),
              (BlockchainURI.LOCKTIME, tx.lock_time.toString)
            ),
            inputTxsList.map(tmp => (BlockchainURI.TX + tmp, BlockchainURI.NEXTOPRETURN))
          )

          //new transaction
          transactionMap.put(tx.hash, outIndexList)
        }

      })
    })

    model.commit()
  }

  /**
    * Delete the dataset
    */
  def delete(): Unit = {
    model.deleteDataset()
  }

}
