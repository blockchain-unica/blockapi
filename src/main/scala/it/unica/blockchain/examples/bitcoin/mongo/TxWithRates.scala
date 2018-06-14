package it.unica.blockchain.examples.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.externaldata.rates.BitcoinRates
import it.unica.blockchain.mongo.Collection
import it.unica.blockchain.utils.converter.DateConverter

/**
  * Created by Livio on 13/06/2017.
  */

object TxWithRates {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase1")

    val txWithRates = new Collection("txWithRates", mongo)

    blockchain.start(400000).end(473100).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)


      block.txs.foreach(tx => {
        txWithRates.append(List(
          ("txHash", tx.hash),
          ("date", block.date),
          ("outputSum", tx.getOutputsSum()),
          ("rate", BitcoinRates.getRate(block.date))
        ))
      })
    })

    txWithRates.close
  }
}
