package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.Exchange
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 16/06/2017.
  */
object TxWithFees {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new MongoSettings("myDatabase")

    val txWithFees = new Collection("txWithFees", mongo)

    blockchain.foreach(block => {

      if(block.height % 1000 == 0){
        println(block.height)
      }

      block.bitcoinTxs.foreach(tx => {
        txWithFees.append(List(
          ("blockHash", block.hash),
          ("txHash", tx.hash),
          ("date", block.date),
          ("fee", tx.getInputsSum() - tx.getOutputsSum()),
          ("rate", Exchange.getRate(block.date))
        ))
      })
    })
  }
}
