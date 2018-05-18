package tcs.examples.ethereum.mongo.duplicatedContracts

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * For each distinct contract, the script adds to the collection contracts:
  * - contractAddress
  * - contractName
  * - date
  * - sourceCode
  *
  * @author Flavia Murru
  * @author Francesca Malloci
  * @author Fabio Carta
  *
  * */

object ContractsCollection {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", true)) //connection
    val mongo = new DatabaseSettings("ethereum") //creates DB mongoDB
    val contracts = new Collection("contracts", mongo) //creates the collection contracts


    blockchain.start(46900).end(500000)foreach(block => {

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
