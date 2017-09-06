package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.OpReturn
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 14/06/2017.
  */
object OpReturnOutputs {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new MongoSettings("myDatabase")

    val opReturnOutputs = new Collection("opReturnOutputs", mongo)

    blockchain.start(290000).foreach(block => {
      if(block.height % 1000 == 0){
        println(block.height)
      }
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          if(out.isOpreturn()) {
            opReturnOutputs.append(List(
              ("txHash", tx.hash),
              ("date", block.date),
              ("protocol", OpReturn.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.outScript.toString)),
              ("metadata", out.getMetadata())
            ))
          }
        })
      })
    })

    opReturnOutputs.close
  }
}