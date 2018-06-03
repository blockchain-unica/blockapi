package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, MySQL}
import tcs.utils.converter.DateConverter._
import tcs.utils._

/**
  * Created by Livio on 14/06/2017.
  */
object TransactionIp {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("alice", "8ak1gI25KFTvjovL3gAM967mies3E=", "8332", MainNet))
    val mySQL = new DatabaseSettings("transactionip", MySQL, "alice", "Djanni74!")

    val startTime = System.currentTimeMillis() / 1000

    val txTable = new Table(sql"""
      create table if not exists transaction(
        txid int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        timestamp TIMESTAMP not null,
        ipRelayedBy varchar(256),
        country varchar(256)
      ) """,
      sql"""insert into transaction(transactionHash, timestamp, ipRelayedBy, country) values (?, ?, ?, ?)""",
      mySQL)

    val testIP = new IP()
    var i = 1
    blockchain.start(421000).end(421000).foreach(block => {
      block.txs.foreach(tx => {

        val ip = tx.getIP()
        val country = testIP.getCountry(ip)

        // insert into table transaction
        txTable.insert(Seq(tx.hash.toString, convertDate(block.date), ip, country))

        // Timesleep necessary to observe the free Blockcypher plan (see https://www.blockcypher.com/dev/faq/)
        Thread.sleep(20000)

        println(i)
        i+=1
        println(ip)
        println(country)
        println("-----------")

      }) // end txs
    }) // end block

    txTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)

  } // end main
} // end object
