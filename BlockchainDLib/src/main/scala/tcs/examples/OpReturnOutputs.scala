package tcs.examples

import tcs.blockchain.BlockchainDlib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.OpReturn
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 14/06/2017.
  */
object OpReturnOutputs {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainDlib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, false))
    val mongo = new MongoSettings("myDatabase")

    val opReturnOutputs = new Collection("opReturnOutputs", mongo)

    blockchain.foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          if(out.isOpreturn()) {
            opReturnOutputs.append(List(
              ("txHash", tx.hash),
              ("date", block.date),
              ("protocol", OpReturn.getProtocol(out.outScript.toString)),
              ("metadata", out.getMetadata())
            ))
          }
        })
      })
    })

  }
}