package it.unica.blockchain.analyses.litecoin.sql

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.externaldata.rates.LitecoinRates
import it.unica.blockchain.utils.converter.DateConverter
import it.unica.blockchain.utils.converter.DateConverter.convertDate


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


    blockchain.start(500000).end(1200000).foreach(block => {

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
