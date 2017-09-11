package tcs.examples

import tcs.db.mysql.Table
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Livio on 14/06/2017.
  */
object TempSQL {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val sql = new DatabaseSettings("myDatabase")

    val opReturnOutputs = new Table("opReturn", sql)

    blockchain.end(480000).foreach(block => {
      println(block.height)
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {

        })
      })
    })

    opReturnOutputs.close
  }
}