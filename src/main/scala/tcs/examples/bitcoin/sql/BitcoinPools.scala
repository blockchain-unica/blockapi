package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, MySQL}
import tcs.utils.DateConverter.convertDate


/**
  * Created by Giancarlo on 07/03/2018.
  */
object BitcoinPools{
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("myblockchain", MySQL, "root", "mysql")

    val txTable = new Table(sql"""
      create table if not exists btcpools(
        blockHash varchar(256) not null,
        timestamp TIMESTAMP not null,
        pool varchar(256) not null
      ) """,
      sql"""insert into btcpools(blockHash, timestamp, pool) values (?, ?, ?)""",
      mySQL)


    blockchain.start(290000).end(300000).foreach(block => {
        txTable.insert(Seq(block.hash.toString(), convertDate(block.date), block.getMiningPool()))
    })

    txTable.close
  }
}