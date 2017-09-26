package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.bitcoin.Exchange
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Livio on 16/06/2017.
  */
object TxWithFees {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("myDatabase")

    val txWithFees = new Collection("txWithFees", mongo)

    blockchain.foreach(block => {
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

    txWithFees.close
  }
}
