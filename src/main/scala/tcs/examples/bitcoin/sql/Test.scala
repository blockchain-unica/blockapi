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
object Test {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("alice", "8ak1gI25KFTvjovL3gAM967mies3E=", "8332", MainNet))
    val mySQL = new DatabaseSettings("offline", MySQL, "alice", "Djanni74!")

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


        // insert into table transaction
       // txTable.insert(Seq(tx.hash.toString, convertDate(block.date), ip, country))

        //Thread.sleep(20000)

        //        val url = "https://api.blockcypher.com/v1/btc/main/txs/f854aebae95150b379cc1187d848d58225f3c4157fe992bcd166f58bd5063449"
        //
        //        println(httprequester.HttpRequester.get(url))
    println("inizio")
    val country = testIP.getCountry("172.31.52.25") // questo mi da geoip vuoto
        println(i)
        i+=1
        println(country)
        println("-----------")

      }) // end txs

    }) // end block

    //txTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)

  } // end main
} // end object
