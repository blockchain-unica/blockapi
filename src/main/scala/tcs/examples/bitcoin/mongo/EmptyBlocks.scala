package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

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
        ("txs", block.bitcoinTxs.size)
      ))
    })

    blocks.close
  }
}