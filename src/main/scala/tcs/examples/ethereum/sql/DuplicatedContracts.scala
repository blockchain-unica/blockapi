package tcs.examples.ethereum.sql

import tcs.db.{DatabaseSettings, MySQL}
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.sql.Table

object DuplicatedContracts {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", true))
    val pg = new DatabaseSettings("ethereum", MySQL, "root", "toor")

    blockchain.start(46400).foreach(block => {

      if (block.height % 100 == 0) {
        println(block.height)
      }

      block.txs.foreach(tx => {

        if (tx.hasContract && tx.contract.sourceCode.length>0) {
          //println(tx.contract.sourceCode.length())
        }

      })
    })

  }

}
