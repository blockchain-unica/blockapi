package it.unica.blockchain.analyses.litecoin.sql
/*
import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.db.{DatabaseSettings, MySQL}

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


    blockchain.start(1200001).foreach(block => {
      if (block.height % 10000 == 0) {
        println(block.height)
      }
      blockTable.insert(Seq(block.hash, block.date, block.getMiningPool(), block.txs.length))

    })

    blockTable.close
  }
}
*/