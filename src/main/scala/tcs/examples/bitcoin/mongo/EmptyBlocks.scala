package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Francesco and Giacomo on 27/02/2018.
  */
object Addresses {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("bitcoinrpc", "smaer1234", "8332", MainNet))
    val mongo = new DatabaseSettings("blocksDB")

    val emptyblocks = new Collection("emptyblocks", mongo)

    // Code here

    emptyblocks.close
  }
}