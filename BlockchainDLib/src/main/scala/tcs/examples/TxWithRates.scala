package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.Exchange
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 13/06/2017.
  */

object TxWithRates {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new MongoSettings("myDatabase")

    val txWithRates = new Collection("txWithRates", mongo)

    blockchain.foreach(block => {
      if (block.height % 1000 == 0) {
        println(block.height)
      }

      block.bitcoinTxs.foreach(tx => {
        txWithRates.append(List(
          ("txHash", tx.hash),
          ("date", block.date),
          ("outputSum", tx.getOutputsSum()),
          ("rate", Exchange.getRate(block.date))
        ))
      })
    })

  }
}
