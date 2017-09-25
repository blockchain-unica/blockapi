package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.mysql.Table
import tcs.db.{DatabaseSettings, MySQL}


/**
  * Created by Livio on 14/06/2017.
  */
object MyBlockchain {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("myblockchain", MySQL, "user", "password")

    var startTimestamp: Long = 0l
    var writingTime: Long = 0l
    var temp: Long = 0l
    var creationTime: Long = 0l
    var closingTime: Long = 0l
    var endTimestamp: Long = 0l
    var totalTime: Long = 0l
    var computationTime: Long = 0l

    startTimestamp = (System.currentTimeMillis / 1000)

    val transactionTable = new Table(sql"""
      create table if not exists transaction(
        txid serial not null PRIMARY key,
        transactionHash varchar(256) not null,
        blockHash varchar(256) not null,
        timestamp TIMESTAMP not null
      )""", mySQL)

    val inputTable = new Table(sql"""
      create table if not exists input(
        id serial not null primary key,
        transactionHash varchar(256) not null,
        inputScript text not null
      )""", mySQL)

    val outputTable = new Table(sql"""
      create table if not exists output(
        id serial not null primary key,
        transactionHash varchar(256) not null,
        outputScript text not null
      )""", mySQL)

    creationTime = (System.currentTimeMillis / 1000) - startTimestamp

    blockchain.end(10000).foreach(block => {
      if(block.height%500==0) println(block.height)

      block.bitcoinTxs.foreach(tx => {
        temp = System.currentTimeMillis / 1000
//        transactionTable.query(sql"insert into transaction (transactionHash, blockHash, timestamp) values (${tx.hash.toString}, ${block.hash.toString}, ${block.date})")
        writingTime += System.currentTimeMillis / 1000 - temp

        tx.inputs.foreach(in => {
          temp = System.currentTimeMillis / 1000
//          inputTable.query(sql"insert into input (transactionHash, inputScript) values (${tx.hash.toString}, ${in.inScript.toString})")
          writingTime += System.currentTimeMillis / 1000 - temp
        })

        tx.outputs.foreach(out => {
          temp = System.currentTimeMillis / 1000
//          outputTable.query(sql"insert into output (transactionHash, outputScript) values (${tx.hash.toString}, ${out.outScript.toString})")
          writingTime += System.currentTimeMillis / 1000 - temp
        })
      })
    })

    temp = System.currentTimeMillis / 1000
    endTimestamp = (System.currentTimeMillis / 1000)
    closingTime = endTimestamp - temp

    totalTime = endTimestamp - startTimestamp
    computationTime = totalTime - creationTime - writingTime - closingTime

    println("Start timestamp: " + startTimestamp + "s")
    println("End timestamp: " + endTimestamp + "s")
    println("Total computation time: " + totalTime + "s\n")

    println("Query-Creation time: " + creationTime + "s (" + (creationTime * 100 / totalTime) + "%)")
    println("Query-Writing time: " + writingTime + "s (" + (writingTime * 100 / totalTime) + "%)")
    println("Query-Closing time: " + closingTime + "s (" + (closingTime * 100 / totalTime) + "%)")
    println("Computation time: " + computationTime + "s (" + (computationTime * 100 / totalTime) + "%)")
  }
}