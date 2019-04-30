package it.unica.blockchain.analyses.litecoin.mongo
/*
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.externaldata.metadata.MetadataParser
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection


object OpReturnOutputsLite {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val opReturnOutputs = new Collection("opReturnLite", mongo)

    blockchain.end(480000).foreach(block => {
      block.txs.foreach(tx => {
        tx.outputs.foreach(out => {
          if(out.isOpreturn()) {
            opReturnOutputs.append(List(
              ("txHash", tx.hash),
              ("date", block.date),
              ("protocol", MetadataParser.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.outScript.toString)),
              ("metadata", out.getMetadata())
            ))
          }
        })
      })
    })

    opReturnOutputs.close
  }
}
*/