package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

object EmptyBlocks {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("emptyBlocksAnalysis")
    val blocks = new Collection("emptyBlocks", mongo)

    // Iterating each block
    blockchain.start(481824).end(500000).foreach(block => {

      if(block.height % 1000 == 0) println(block.height)

      blocks.append(List(
        ("chain", "B"),
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