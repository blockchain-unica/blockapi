package it.unica.blockchain.analyses.ethereum.mongo.duplicatedContracts

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

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
            ("contractAddress", tx.contract.address.address),
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
