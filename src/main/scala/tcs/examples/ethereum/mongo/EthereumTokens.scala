package tcs.examples.ethereum.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumContract
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings

import tcs.mongo.Collection

object EthereumTokens {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("https://mainnet.infura.io/XerLZnGLLqs26v1mQZAE:8545"))
    val mongo = new DatabaseSettings("EthereumTokens")
    val blocks = new Collection("EthereumTokens", mongo)

    blockchain.start(1196010).end(1196020).foreach(block => {

      block.txs.foreach(tx => {

        if (tx.hasContract){
          if(tx.contract.erc20Compliant){
            val list = List(
              ("contractAddress", tx.contract.address),
              ("contractName", tx.contract.name),
              ("date", block.date),
              ("dateVerified", tx.contract.verificationDate),
              ("sourceCode", tx.contract.sourceCode),
              ("usesPermissions", tx.contract.usesPermissions)
            )
            blocks.append(list)
          }
        }

      })

    })

  }
}
