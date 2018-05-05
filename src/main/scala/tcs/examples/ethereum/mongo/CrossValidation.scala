package tcs.examples.ethereum.mongo

import tcs.db.DatabaseSettings
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.mongo.Collection

object CrossValidation {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val myBlockchain = new Collection("ethCrossValidation", mongo)

    //Chiedere se gli estremi dei blocchi devono essere passati in input o hardcoded
    blockchain.start(2427000).end(2427784).foreach(block => {
      if (block.height % 100 == 0) {
        println("Current block ->" + block.height)
      }

      block.txs.foreach(tx => {
        val internalTransactions = block.internalTransactions.filter(itx => itx.parentTxHash.equals(tx.hash))
        val creates = if (tx.addressCreated == null) "" else tx.addressCreated
        val to = if (tx.to == null) "" else tx.to
        val list = List(
          ("txHash", tx.hash), //Richiesto
          ("blockHeight", tx.blockHeight.toString()),
          ("txIndex", tx.transactionIndex),
          ("date", block.date), //Richiesto
          ("from", tx.from),
          ("to", to),
          ("value", tx.value.doubleValue() / weiIntoEth.doubleValue()), //Richiesto
          ("creates", creates),
          ("internalTransactions", internalTransactions),
          ("hasContract", tx.hasContract) //Richiesto
        )
        myBlockchain.append(list)
      })
    })

    myBlockchain.close
  }
}
