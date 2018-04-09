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

  }
}
