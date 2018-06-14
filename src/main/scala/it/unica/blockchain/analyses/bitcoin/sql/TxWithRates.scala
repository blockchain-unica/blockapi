package it.unica.blockchain.analyses.bitcoin.sql

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.utils.converter.DateConverter.convertDate
import it.unica.blockchain.utils.converter.DateConverter
import it.unica.blockchain.externaldata.rates.BitcoinRates

/**
  * Created by Stefano on 02/11/2017.
  */
object TxWithRates {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("rates", MySQL, "user", "password")


    val startTime = System.currentTimeMillis() / 1000

    val txTable = new Table(
      sql"""
      create table if not exists txrates(
        id serial not null primary key,
        txHash varchar(256) not null,
        txdate TIMESTAMP not null,
        outputsum bigint,
        rate float
    )""",
      sql"""insert into txrates (txHash, txdate, outputsum, rate) values(?,?,?,?)""",
      mySQL)


    blockchain.end(473100).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        txTable.insert(Seq(
          tx.hash.toString,
          convertDate(block.date),
          tx.getOutputsSum(),
          BitcoinRates.getRate(block.date)))
      })
    })

    txTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
