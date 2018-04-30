package tcs.examples.ethereum.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.{EthereumSettings, EthereumTransaction}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object EthereumTokens {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("EthereumTokens")
    val tokens = new Collection("EthereumTokens", mongo)

    // Iterating each block
    blockchain.start(5061244).end(5061444).foreach(block => {
      if(block.height%100 == 0){
        println("Current Block " + block.height)
      }
      block.txs.foreach(tx => {
          if (tx.hasContract && tx.contract.isERC20Compliant){

            tokens.append(
              List(
                ("contractAddress", tx.contract.address),
                ("txhash",tx.contract.hashOriginatingTx),
                ("date",tx.date),
                //("bytecode", tx.contract.bytecode),
                ("tokenName", tx.contract.getTokenName()),
                ("tokenSymbol", tx.contract.getTokenSymbol()),
                ("tokenDivisibility", tx.contract.getTokenDivisibility())
              )
            )
          }
      })
    })
    tokens.close
  }
}