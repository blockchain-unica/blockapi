package tcs.examples.ethereum.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object EmptyBlocks {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("emptyBlocksAnalysis")
    val blocks = new Collection("emptyBlocks", mongo)

    // Iterating each block
    blockchain.start(1800001).end(2000000).foreach(block => {

      if (block.height % 1000 == 0) println(block.height)

      blocks.append(List(
        ("chain", "E"),
        ("hash", block.hash),
        ("height", block.height),
        ("pool", block.getMiningPool),
        ("date", block.date),
        ("txs", block.txs.size)
      ))
    })

    blocks.close
  }
}
