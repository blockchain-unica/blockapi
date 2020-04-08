package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import it.unica.blockchain.utils.converter.DateConverter
import it.unica.blockchain.externaldata.rates.BitcoinRates

/**
  * Created by Livio on 16/06/2017.
  */

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object TxWithFees {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("myDatabase")

    val txWithFees = new Collection("txWithFees", mongo)

    /**This analysis must be runned without setting the starting point.*/
    blockchain.end(1000).foreach(block => {

      println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        val fee = tx.getInputsSum() - tx.getOutputsSum()
        txWithFees.append(List(
          ("blockHash", block.hash),
          ("txHash", tx.hash),
          ("date", block.date),
          ("fee", if(fee < 0) -1 else fee),
          ("rate", BitcoinRates.getRate(block.date))
        ))
      })
    })

    txWithFees.close
  }
}
