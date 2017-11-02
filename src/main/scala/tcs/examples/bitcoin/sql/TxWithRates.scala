package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.bitcoin.Exchange
import tcs.db.{DatabaseSettings, MySQL}
import tcs.db.mysql.Table
import tcs.utils.DateConverter
import tcs.utils.DateConverter.convertDate

/**
  * Created by Stefano on 02/11/2017.
  */
object TxWithRates {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mySQL = new DatabaseSettings("fees", MySQL, "user", "password")


    val startTime = System.currentTimeMillis() / 1000

    val txTable = new Table(
      sql"""
      create table if not exists txfees(
        id serial not null primary key,
        txHash varchar(256) not null,
        txdate TIMESTAMP not null,
        outputsum bigint,
        rate float
    )""",
      sql"""insert into txfees (txHash, txdate, outputsum, rate) values(?,?,?,?)""",
      mySQL)


    blockchain.end(473100).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.bitcoinTxs.foreach(tx => {
        txTable.insert(Seq(
          tx.hash.toString,
          convertDate(block.date),
          tx.getOutputsSum(),
          Exchange.getRate(block.date)))
      })
    })

    txTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
