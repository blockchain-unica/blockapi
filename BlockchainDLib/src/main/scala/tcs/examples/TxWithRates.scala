package tcs.examples

import tcs.blockchain.BlockchainDlib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.Exchange
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 13/06/2017.
  */

object TxWithRates {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainDlib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, false))
    val mongo = new MongoSettings("myDatabase")

    val txWithRates = new Collection("txWithRates", mongo)

    blockchain.foreach(block => {
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
