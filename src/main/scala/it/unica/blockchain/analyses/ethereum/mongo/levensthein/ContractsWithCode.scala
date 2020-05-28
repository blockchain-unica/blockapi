package it.unica.blockchain.analyses.ethereum.mongo.levensthein

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.{EthereumBlockchain, EthereumSettings}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

object ContractsWithCode {
  def main(args: Array[String]): Unit = {
    val blockchain: EthereumBlockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val contractsWithCode = new Collection("contractsWithCode", mongo)

    blockchain.start(1000000).end(1001000).foreach(block => {
      if (block.height % 100 == 0) {
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        if (tx.hasContract) {
          val list = List(
            ("contractAddress", tx.addressCreated.address),
            ("contractCode", tx.contract.bytecode)
          )
          contractsWithCode.append(list)
        }
      })
    })
    contractsWithCode.close
  }
}
