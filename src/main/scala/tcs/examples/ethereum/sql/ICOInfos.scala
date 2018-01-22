package tcs.examples.ethereum.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.db.mysql.Table
import tcs.db.{DatabaseSettings, PostgreSQL}


object ICOInfos {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain("http://localhost:8545")
      .setStart(0).setEnd(1370000)
    val pg = new DatabaseSettings("ethereum", PostgreSQL, "postgres")

    val icoTable = new Table(
      sql"""
         CREATE TABLE IF NOT EXISTS ico(
          icoId INTEGER NOT NULL PRIMARY KEY,
          tokenName CHARACTER VARYING(25),
          tokenSymbol CHARACTER VARYING(10),
          contractId INTEGER NOT NULL,
          marketCap NUMERIC(20,2),
          totalSupply NUMERIC(20,2),
          blockchain CHARACTER VARYING(15),
          priceUSD NUMERIC(8,8),
          priceETH NUMERIC(8,8),
          priceBTC NUMERIC(8,8),
          hypeScore NUMERIC(3,2),
          riskScore NUMERIC(3,2),
          investmentRating CHARACTER VARYING(10)
         ) """,
      sql"""
          INSERT INTO ico(tokenName, tokenSymbol, contractId, marketCap,
          totalSupply, blockchain, priceUSD, priceETH, priceBTC,
          hypeScore, riskScore, investmentRating)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         """,
      pg)

    val txTable = new Table(
      sql"""

         """,
      sql"""
         """,
      pg
    )
  }
}
