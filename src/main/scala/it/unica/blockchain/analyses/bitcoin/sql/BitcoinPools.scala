package it.unica.blockchain.analyses.bitcoin.sql

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.utils.converter.DateConverter.convertDate


/**
  * Created by Giancarlo on 07/03/2018.
  */
object BitcoinPools{
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("myblockchain", MySQL, "user", "password")

    val txTable = new Table(sql"""
      create table if not exists btcpools(
        blockHash varchar(256) not null,
        timestamp TIMESTAMP not null,
        pool varchar(256) not null
      ) """,
      sql"""insert into btcpools(blockHash, timestamp, pool) values (?, ?, ?)""",
      mySQL)


    blockchain.start(200000).end(300000).foreach(block => {
        txTable.insert(Seq(block.hash.toString(), convertDate(block.date), block.getMiningPool()))
        println("Done working on block @ height " + block.height)
    })

    txTable.close
  }
}