package it.unica.blockchain.examples.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import it.unica.blockchain.utils.converter.DateConverter
import it.unica.blockchain.externaldata.rates.BitcoinRates

/**
  * Created by Livio on 16/06/2017.
  */
object TxWithFees {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("myDatabase")

    val txWithFees = new Collection("txWithFees", mongo)

    blockchain.end(473100).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        txWithFees.append(List(
          ("blockHash", block.hash),
          ("txHash", tx.hash),
          ("date", block.date),
          ("fee", tx.getInputsSum() - tx.getOutputsSum()),
          ("rate", BitcoinRates.getRate(block.date))
        ))
      })
    })

    txWithFees.close
  }
}
