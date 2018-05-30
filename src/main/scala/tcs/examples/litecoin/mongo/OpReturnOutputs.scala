package tcs.examples.litecoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.litecoin.{LitecoinSettings, MainNet}
import tcs.externaldata.metadata.MetadataParser
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Giulia on 14/06/2017.
  */
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