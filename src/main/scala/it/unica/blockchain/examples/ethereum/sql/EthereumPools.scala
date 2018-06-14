package it.unica.blockchain.examples.ethereum.sql

import java.util.Date

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.db.{DatabaseSettings, PostgreSQL}
import scalikejdbc._
import it.unica.blockchain.blockchains.ethereum.EthereumSettings

object EthereumPools {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("https://mainnet.infura.io/lGhdnAJw7n56K0xXGP3i:8545"))
    val pg = new DatabaseSettings("ethereum", PostgreSQL, "postgres", "password")
    val blockTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS pools(
            hash CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
            timestamp TIMESTAMP,
            mining_pool CHARACTER VARYING(50)
          )
         """,
      sql"""
          INSERT INTO pools(hash,timestamp, mining_pool) VALUES (?, ?, ?)
         """,
      pg, 100
    )

    blockchain.foreach(block => {

      if (block.height % 100 == 0) {
        println(block.height)
      }

      blockTable.insert(Seq(block.hash, block.date, block.getMiningPool))

    })

    blockTable.close
  }
}
