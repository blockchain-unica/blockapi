package it.unica.blockchain.analyses.ethereum.sql

import java.util.Date

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.db.{DatabaseSettings, PostgreSQL}
import scalikejdbc._
import it.unica.blockchain.blockchains.ethereum.EthereumSettings

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object EthereumPools {
  def main(args: Array[String]): Unit = {
    /** Signup to Infura and insert your Project ID into the URL, after "https://mainnet.infura.io/" **/
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("https://mainnet.infura.io/InsertYourProjectID:8545"))
    val pg = new DatabaseSettings("ethereum", PostgreSQL, "postgres", "password")
    val blockTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS pools(
            hash VARCHAR(100) NOT NULL PRIMARY KEY,
            timestamp TIMESTAMP,
            mining_pool VARCHAR(50)
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
