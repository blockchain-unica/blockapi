package it.unica.blockchain.analyses.ethereum.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import it.unica.blockchain.externaldata.rates.EthereumRates

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object TxWithRates {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val txWithRates = new Collection("txWithRates", mongo)

    blockchain.end(150000).foreach(block => {
      if(block.height % 1000 == 0){
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        val creates = if(tx.hasContract) tx.addressCreated else ""
        val to = if(tx.to == null) "" else tx.to
        val list = List(
          ("txHash", tx.hash),
          ("blockHeight", tx.blockHeight.toString()),
          ("txIndex", tx.transactionIndex),
          ("date", block.date),
          ("from", tx.from),
          ("to", to),
          ("value", tx.value.doubleValue()/weiIntoEth.doubleValue()),
          ("creates", creates),
          ("rate", EthereumRates.getRate(block.date))
        )
        txWithRates.append(list)
      })
    })

    txWithRates.close
  }
}
