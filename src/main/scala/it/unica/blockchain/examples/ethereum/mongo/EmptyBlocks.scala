package it.unica.blockchain.examples.ethereum.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

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
