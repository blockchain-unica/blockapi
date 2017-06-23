package tcs.examples

import tcs.blockchain.BlockchainDlib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.Exchange
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 16/06/2017.
  */
object TxWithFees {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainDlib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new MongoSettings("myDatabase")

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
  }
}
