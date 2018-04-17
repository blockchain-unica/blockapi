package tcs.examples.ethereum.mongo.duplicatedContracts

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object ContractsCollection {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", true)) //connection
    val mongo = new DatabaseSettings("ethereum") //creates DB mongoDB
    val contracts = new Collection("contracts", mongo) //creates the collection


    blockchain.start(510600).end(510800)foreach(block => {

      if (block.height % 100 == 0) {
        println("Current block:   " + block.height)
      }

      block.txs.foreach(tx => {

        if (tx.hasContract && tx.contract.sourceCode.length > 0) {
          val list = List(
            ("contractAddress", tx.contract.address),
            ("contractName", tx.contract.name),
            ("date", block.date),
            ("sourceCode", tx.contract.sourceCode)
          )
          contracts.append(list)
        }
      })
    })

    contracts.close
  }
}
