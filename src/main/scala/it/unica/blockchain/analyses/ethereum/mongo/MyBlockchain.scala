package it.unica.blockchain.analyses.ethereum.mongo

import java.util.Date

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

object MyBlockchain {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val myBlockchain = new Collection("myBlockchain", mongo)

    blockchain.start(5350000).end(5367585).foreach(block => {
      if(block.height % 100 == 0){
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        val internalTransactions = block.internalTransactions.filter(itx => itx.parentTxHash.equals(tx.hash))
        val creates = if(tx.addressCreated == null) "" else tx.addressCreated.address
        val to = if(tx.to == null) "" else tx.to.address
        val list = List(
          ("txHash", tx.hash),
          ("blockHeight", tx.blockHeight.toString()),
          ("txIndex", tx.transactionIndex),
          ("date", block.date),
          ("from", tx.from.address),
          ("to", to),
          ("value", tx.value.doubleValue()/weiIntoEth.doubleValue()),
          ("creates", creates),
          ("internalTransactions", internalTransactions)
        )
        myBlockchain.append(list)
      })
    })

    myBlockchain.close
  }
}
