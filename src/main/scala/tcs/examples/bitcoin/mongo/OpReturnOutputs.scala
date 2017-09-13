package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Livio on 14/06/2017.
  */
object OpReturnOutputs {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val opReturnOutputs = new Collection("opReturn", mongo)

    blockchain.end(480000).foreach(block => {
//      if(block.height % 500 == 0){
        println(block.height)
//      }
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          if(out.isOpreturn()) {
/*            opReturnOutputs.append(List(
              ("txHash", tx.hash),
              ("date", block.date),
              ("protocol", OpReturn.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.outScript.toString)),
              ("metadata", out.getMetadata())
            )) */
          }
        })
      })
    })

    opReturnOutputs.close
  }
}