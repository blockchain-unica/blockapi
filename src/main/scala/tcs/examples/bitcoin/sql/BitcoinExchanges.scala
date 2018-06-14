package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.{DatabaseSettings, PostgreSQL}
import tcs.db.sql.Table
import tcs.utils.converter.DateConverter
import tcs.externaldata.rates.BitcoinRates


object  BitcoinExchanges {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val postgreSQL = new DatabaseSettings("btcExchanges", PostgreSQL, "postgres", "password")
    val startTime = System.currentTimeMillis() / 1000

    val txTable = new Table(
      sql"""
      create table if not exists txrates(
        id serial not null primary key,
        txHash varchar(256) not null,
        txdate TIMESTAMP not null,
        outputsum bigint,
        USDrate float,
        EURrate float,
        GBPrate float,
        JPYrate float,
        CNYrate float
    )""",
      sql"""insert into txrates (txHash, txdate, outputsum, USDrate, EURrate, GBPrate, JPYrate, CNYrate) values(?,?,?,?,?,?,?,?)""",
      postgreSQL)

    blockchain.start(232901).end(235800).foreach(block => {

      if (block.height % 10000 == 0)
      println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Tx: " + tx.date)
        txTable.insert(Seq(
          tx.hash.toString,
          block.date,
          tx.getOutputsSum(),
          BitcoinRates.getRate(block.date, "USD"),
          BitcoinRates.getRate(block.date, "EUR"),
          BitcoinRates.getRate(block.date, "GBP"),
          BitcoinRates.getRate(block.date, "JPY"),
          BitcoinRates.getRate(block.date, "CNY")
        ))
      })
    })

    txTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
