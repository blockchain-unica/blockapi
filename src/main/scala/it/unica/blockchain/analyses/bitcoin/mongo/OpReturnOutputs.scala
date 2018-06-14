package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.externaldata.metadata.MetadataParser
import it.unica.blockchain.mongo.Collection

/**
  * Created by Livio on 14/06/2017.
  */
object OpReturnOutputs {
  def main(args: Array[String]): Unit ={

    //val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("bitcoin", "L4mbWnzC35BNrmTJ", "80", "co2.unica.it", MainNet))
    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))

    val mongo = new DatabaseSettings("myDatabase1")

    val opReturnOutputs = new Collection("opReturn", mongo)

    blockchain.start(290000).end(480000).foreach(block => {
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