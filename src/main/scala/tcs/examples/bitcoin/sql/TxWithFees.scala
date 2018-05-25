package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.{DatabaseSettings, MySQL}
import tcs.db.sql.Table
import tcs.utils.converter.DateConverter.convertDate
import tcs.utils.converter.DateConverter
import tcs.externaldata.rates.BitcoinRates

/**
  * Created by Livio on 28/09/2017.
  */
object TxWithFees {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mySQL = new DatabaseSettings("fees", MySQL, "user", "password")


    val startTime = System.currentTimeMillis() / 1000

    val txTable = new Table(
      sql"""
      create table if not exists txfees(
        id serial not null primary key,
        blockHash varchar(256) not null,
        transactionHash varchar(256) not null,
        txdate TIMESTAMP not null,
        fee bigint,
        rate float
    )""",
      sql"""insert into txfees (blockHash, transactionHash, txdate, fee, rate) values(?,?,?,?,?)""",
      mySQL)


    blockchain.foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        txTable.insert(Seq(
          block.hash.toString,
          tx.hash.toString,
          convertDate(block.date),
          (tx.getInputsSum() - tx.getOutputsSum()),
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
