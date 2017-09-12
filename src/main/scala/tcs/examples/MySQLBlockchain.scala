package tcs.examples

import scalikejdbc._
import tcs.db.mysql.Table
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.{DatabaseSettings, MySQL}


/**
  * Created by Livio on 14/06/2017.
  */
object MySQLBlockchain {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("myDatabase", MySQL, "user", "password")

    val tableName = "mySQLBlockchain"
    val query = sql"""
      create table tableName (
        transactionHash varchar(256) not null primary key,
        blockHash varchar(256) not null
      )"""
    val opReturn = new Table(tableName, query, mySQL)


    blockchain.end(10).foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        opReturn.insert(sql"insert into tableName (transactionHash, blockHash) values (${tx.hash}, ${block.hash})")
      })
    })

    opReturn.close
  }
}