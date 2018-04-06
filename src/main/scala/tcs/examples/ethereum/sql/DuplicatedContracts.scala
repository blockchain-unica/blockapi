package tcs.examples.ethereum.sql

import scalikejdbc._
import tcs.db.{DatabaseSettings, MySQL}
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.sql.Table

object DuplicatedContracts {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", true))
    val pg = new DatabaseSettings("ethereum", MySQL, "root", "toor")

    val contractTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS contract(
            address CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
            source_code LONGTEXT NOT NULL,
            date TIMESTAMP NOT NULL,
            name CHARACTER VARYING(100) NOT NULL
          )
         """,
      sql"""
          INSERT INTO contract(address, source_code, date, name) VALUES (?, ?, ?, ?)
         """,
      pg, 1
    )

    blockchain.start(49400).foreach(block => {

      if (block.height % 100 == 0) {
        println(block.height)
      }

      block.txs.foreach(tx => {

        if (tx.hasContract && tx.contract.sourceCode.length>0) {
          contractTable.insert(Seq(tx.contract.address, tx.contract.sourceCode, block.date, tx.contract.name));

        }

      })
    })

    contractTable.close
  }

}
