package it.unica.blockchain.analyses.ethereum.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import it.unica.blockchain.externaldata.rates.EthereumRates

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object TxWithFees {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val txWithFees = new Collection("txWithFees", mongo)

    blockchain.start(5350000).end(5367585).foreach(block => {
      if(block.height % 100 == 0){
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        val creates = if(tx.hasContract) tx.addressCreated else ""
        val to = if(tx.to == null) "" else tx.to
        val list = List(
          ("blockHash", block.hash),
          ("txHash", tx.hash),
          ("date", block.date),
          ("value", tx.value.doubleValue()/weiIntoEth.doubleValue()),
          ("creates", creates),
          ("gas", tx.gas),
          ("fee", (tx.gas * tx.gasPrice).doubleValue()/weiIntoEth.doubleValue()),
          ("rate", EthereumRates.getRate(block.date))
        )
        txWithFees.append(list)
      })
    })

    txWithFees.close
  }
}
