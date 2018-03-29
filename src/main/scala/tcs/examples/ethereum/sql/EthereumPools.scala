package tcs.examples.ethereum.sql

import java.util.Date

import tcs.blockchain.BlockchainLib
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, PostgreSQL}
import scalikejdbc._
import tcs.blockchain.ethereum.EthereumSettings

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

      if (block.number % 100 == 0) {
        println(block.number)
      }

      blockTable.insert(Seq(block.hash, new Date(block.timeStamp.longValue() * 1000), block.getMiningPool))

    })

    blockTable.close
  }
}
