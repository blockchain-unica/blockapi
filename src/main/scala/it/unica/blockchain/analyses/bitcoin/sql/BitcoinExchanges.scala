package it.unica.blockchain.analyses.bitcoin.sql

import java.util.Date

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, PostgreSQL}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.utils.converter.DateConverter
import it.unica.blockchain.externaldata.rates.BitcoinRates

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object  BitcoinExchanges {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val postgreSQL = new DatabaseSettings("btcExchanges", PostgreSQL, "user", "password")
    val startTime = System.currentTimeMillis() / 1000
    val start = 68750
    val end = 68850

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


    //Settings for rates
    //Getting start date
    val start_date = blockchain.getBlock(start).date
    //Getting end date
    val end_date = blockchain.getBlock(end).date

    // Coindesk has no rates before this timestamp
    if (!end_date.before(new Date(1279411200000l))) BitcoinRates.setRate(start_date, end_date)

    blockchain.start(start).end(end).foreach(block => {

      if (block.height % 10000 == 0)
        println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      //Take rates values for the currencies
      println("Getting rates...")
      val exMap = BitcoinRates.getRate_Mod(block.date)

      block.txs.foreach(tx => {
        println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Tx: " + tx.date)
        txTable.insert(Seq(
          tx.hash.toString,
          block.date,
          tx.getOutputsSum(),
          exMap.get("USD"),
          exMap.get("EUR"),
          exMap.get("GBP"),
          exMap.get("JPY"),
          exMap.get("CNY")
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
