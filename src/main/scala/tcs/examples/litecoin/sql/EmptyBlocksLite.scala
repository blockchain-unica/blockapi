package tcs.examples.litecoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.litecoin.{LitecoinSettings, MainNet}
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, MySQL}

/**Created by Giulia on 11/06/2018
  */

object EmptyBlocksLite {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9332", MainNet))
    val mySQL = new DatabaseSettings("emptyblockslite", MySQL, "user", "password")

    val blockTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS emptyblockanalysis(
            hash CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
            timestamp DATETIME,
            miner CHARACTER VARYING(100),
            txsnumber INTEGER
          )""",
      sql""" insert into emptyblockanalysis(hash,timestamp, miner, txsnumber) values (?, ?, ?, ?)""",
      mySQL)


    blockchain.start(800000).end(1200000).foreach(block => {
      if (block.height % 10000 == 0) {
        println(block.height)
      }
      blockTable.insert(Seq(block.hash, block.date, block.getMiningPool(), block.txs.length))

    })

    blockTable.close
  }
}
