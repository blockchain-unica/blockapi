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

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("bitcoinrpc", "smaer1234", "8332", MainNet))
    val mongo = new DatabaseSettings("blocksDB")

    val emptyBlocks = new Collection("emptyblocks", mongo)

    // Iterating each block
    blockchain.foreach(block => {
      if (block.bitcoinTxs.length == 1) { // checking if the block it's "empty" (i.e. only the coinbase tx)
        emptyBlocks.append(List(
          ("blockHash", block.hash),
          ("date", block.date)
        ))
      }
      
      emptyBlocks.close
    })
  }
}