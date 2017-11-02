package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.bitcoin.Exchange
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.DateConverter

/**
  * Created by Livio on 13/06/2017.
  */

object TxWithRates {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val txWithRates = new Collection("txWithRates", mongo)

    blockchain.end(473100).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)


      block.bitcoinTxs.foreach(tx => {
        txWithRates.append(List(
          ("txHash", tx.hash),
          ("date", block.date),
          ("outputSum", tx.getOutputsSum()),
          ("rate", Exchange.getRate(block.date))
        ))
      })
    })

    txWithRates.close
  }
}
