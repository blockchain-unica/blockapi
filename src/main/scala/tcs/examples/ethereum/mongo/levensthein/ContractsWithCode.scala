package tcs.examples.ethereum.mongo.levensthein

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.{EthereumBlockchain, EthereumSettings}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object ContractsWithCode {
  def main(args: Array[String]): Unit = {
    val blockchain: EthereumBlockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val contractsWithCode = new Collection("contractsWithCode", mongo)

    blockchain.start(1000000).end(1200000).foreach(block => {
      if (block.height % 100 == 0) {
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        if (tx.addressCreated != null) {
          val list = List(
            ("contractAddress", tx.addressCreated),
            ("contractCode", blockchain.getContractCode(tx.addressCreated))
          )
          contractsWithCode.append(list)
        }
      })
    })
    contractsWithCode.close
  }
}
