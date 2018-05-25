package tcs.examples.litecoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.litecoin.{LitecoinSettings, MainNet}
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, MySQL}
import tcs.externaldata.rates.LitecoinRates
import tcs.utils.converter.DateConverter
import tcs.utils.converter.DateConverter.convertDate


object TxWithRatesLite {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9332", MainNet))
    val mySQL = new DatabaseSettings("rates", MySQL, "user", "password")


    val startTime = System.currentTimeMillis() / 1000

    val txTable = new Table(
      sql"""
      create table if not exists txrateslite(
        id serial not null primary key,
        txHash varchar(256) not null,
        txdate TIMESTAMP not null,
        outputsum bigint,
        rate float
    )""",
      sql"""insert into txrateslite (txHash, txdate, outputsum, rate) values(?,?,?,?)""",
      mySQL)


    blockchain.end(100000).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        txTable.insert(Seq(
          tx.hash.toString,
          convertDate(block.date),
          tx.getOutputsSum(),
          LitecoinRates.getRate(block.date)))
      })
    })

    txTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
