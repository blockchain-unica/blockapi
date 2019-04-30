package it.unica.blockchain.analyses.ethereum.mongo

import java.util.Date

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

object EVM {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")

    val evm = new Collection("evm", mongo)
    val transactions = new Collection("transactions", mongo)

    blockchain.end(1000000).foreach(block => {
      if(block.height % 100 == 0){
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        val internalTransactions = block.internalTransactions.filter(itx => itx.parentTxHash.equals(tx.hash))
        val creates = if(tx.addressCreated == null) "" else tx.addressCreated
        val to = if(tx.to == null) "" else tx.to
        transactions.append(
          List(
            ("block", block.height),
            ("txHash", tx.hash),
            ("from", tx.from),
            ("to", to),
            ("value", tx.value)
          )
        )

        if(tx.hasContract){
          evm.append(
            List(
              ("txhash", tx.contract.hashOriginatingTx),
              ("contractAddress", tx.contract.address),
              ("evm", tx.contract.bytecode)
            )
          )
        }
      })
    })

    evm.close
    transactions.close
  }
}
