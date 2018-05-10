package tcs.examples.ethereum.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object EthereumERC20Info {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("EthereumTokens")
    val tokens = new Collection("EthereumTokens", mongo)


    // Iterating each block
    blockchain.start(1703600).end(2100000).foreach(block => {
      if(block.height%100 == 0){
        println("Current Block " + block.height)
      }

      if (block.internalTransactions != List.empty){

        block.internalTransactions.foreach(itx => {
            println(itx)
        })

      } else {
        //println("No internal transaction")  //testing code
      }

      block.txs.foreach(tx => {
        if (tx.hasContract && tx.contract.isERC20Compliant){



        }
      })
    })
    tokens.close
  }

}
