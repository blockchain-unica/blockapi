package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Francesco and Giacomo on 27/02/2018.
  */
object EmptyBlocks {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("blocksDB")

    val blocks = new Collection("blocks", mongo) // Collection for all blocks (used for analysis)
    val emptyBlocks = new Collection("emptyblocks", mongo) // Collection for empty blocks

    // Iterating each block
    blockchain.foreach(block => {
      // checking if the block it's "empty" (i.e. only the coinbase tx)
      if (block.bitcoinTxs.lengthCompare(1) == 0)
        // adding the empty block into the collection emptyblocks
        emptyBlocks.append(List(
          ("blockHash", block.hash),
          ("date", block.date)
        ))
        // Adding the block into the collection blocks
        blocks.append(List(
          ("blockHash", block.hash),
          ("date", block.date)
        ))
    })

    blocks.close
    emptyBlocks.close
  }
}