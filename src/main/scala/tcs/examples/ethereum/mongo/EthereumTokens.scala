package tcs.examples.ethereum.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.{EthereumSettings, EthereumTransaction}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/** Get a set of transactions, for each ERC20 standard contract,
  * this script finds:
  * - contract address
  * - hash of originating transaction
  * - date
  * - name of the token
  * - symbol of the token
  * - divisibility
  *
  *
  * @author Chessa Stefano Raimondo
  * @author Guria Marco
  * @author Manai Alessio
  * @author Speroni Alessio
  * */

object EthereumTokens {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("EthereumTokens")
    val tokens = new Collection("EthereumTokens", mongo)

    // Iterating each block
    blockchain.start(5526000).end(5530777).foreach(block => {
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