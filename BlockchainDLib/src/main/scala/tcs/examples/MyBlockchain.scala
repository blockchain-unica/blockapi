package tcs.examples

import tcs.blockchain.BlockchainDlib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by stefano on 13/06/17.
  */
object MyBlockchain {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainDlib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, false))
    val mongo = new MongoSettings("myDatabase")

    val myBlockchain = new Collection("myBlockchain", mongo)

    blockchain.foreach(block => {
      if(block.height % 1000 == 0){
        println(block.height)
      }
      block.bitcoinTxs.foreach(tx => {
        myBlockchain.append(List(
          ("txHash", tx.hash),
          ("blockHash", block.hash),
          ("date", block.date),
          ("inputs", tx.inputs),
          ("outputs", tx.outputs)
        ))
      })
    })

  }
}
