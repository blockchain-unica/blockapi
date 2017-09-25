package tcs.examples.bitcoin.sql

import java.util.Date

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.mysql.Table
import tcs.db.{DatabaseSettings, MySQL}

import scala.collection.mutable.ListBuffer

/**
  * Created by Livio on 14/06/2017.
  */
object TestMyBlockchain{
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("myblockchain", MySQL, "user", "password")

    var startTimestamp: Long = 0l
    var writingTime: Long = 0l
    var temp: Long = 0l
    var creationTime: Long = 0l
    var endTimestamp: Long = 0l
    var totalTime: Long = 0l
    var computationTime: Long = 0l

    startTimestamp = (System.currentTimeMillis / 1000)

    val transactionTable = new Table(sql"""
      create table if not exists transaction(
        txid int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        blockHash varchar(256) not null,
        timestamp varchar(40) not null
      ) """, mySQL)

    val inputTable = new Table(sql"""
      create table if not exists input(
        id int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        inputScript text not null
      ) """, mySQL)

    val outputTable = new Table(sql"""
      create table if not exists output(
        id int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        outputScript text not null
      ) """, mySQL)

    creationTime = (System.currentTimeMillis / 1000) - startTimestamp

    var transactionParams = ListBuffer[(java.lang.String, java.lang.String, Date)]()
    var inputParams = ListBuffer[(java.lang.String, java.lang.String)]()
    var outputParams = ListBuffer[(java.lang.String, java.lang.String)]()

    val lastBlock = 100000 // 473100

    blockchain.end(lastBlock).foreach(block => {
      block.bitcoinTxs.foreach(tx => {

        transactionParams += ((tx.hash.toString, block.hash.toString, block.date))

        tx.inputs.foreach(in => { inputParams += ((tx.hash.toString, in.inScript.toString)) })

        tx.outputs.foreach(out => { outputParams += ((tx.hash.toString, out.outScript.toString)) })
      })

      if(transactionParams.size >= 50000){
        println("Working at " + block.height)
        temp = System.currentTimeMillis / 1000
        writeDB(transactionParams, inputParams, outputParams, transactionTable, inputTable, outputTable)
        writingTime += System.currentTimeMillis / 1000 - temp
        println("Time: " + ((System.currentTimeMillis / 1000) - temp))
      }
    })

    // Write last values
    println("Closing at " + lastBlock)
    temp = System.currentTimeMillis / 1000
    writeDB(transactionParams, inputParams, outputParams, transactionTable, inputTable, outputTable)
    writingTime += System.currentTimeMillis / 1000 - temp
    println("Time: " + ((System.currentTimeMillis / 1000) - temp))

    endTimestamp = System.currentTimeMillis / 1000
    totalTime = endTimestamp - startTimestamp
    computationTime = totalTime - creationTime - writingTime

    println("Start timestamp: " + startTimestamp + "s")
    println("End timestamp: " + endTimestamp + "s")
    println("Total computation time: " + totalTime + "s\n")

    println("Query-Creation time: " + creationTime + "s (" + (creationTime * 100 / totalTime) + "%)")
    println("Query-Writing time: " + writingTime + "s (" + (writingTime * 100 / totalTime) + "%)")
    println("Computation time: " + computationTime + "s (" + (computationTime * 100 / totalTime) + "%)")
  }


  def writeDB(
             transactionParams : ListBuffer[(java.lang.String, java.lang.String, Date)],
             inputParams : ListBuffer[(java.lang.String, java.lang.String)],
             outputParams : ListBuffer[(java.lang.String, java.lang.String)],
             transactionTable : Table,
             inputTable : Table,
             outputTable : Table
           ): Unit ={

      val batchTxParams: Seq[Seq[Any]] = transactionParams.map(i => Seq(i._1, i._2, i._3.toString))
      transactionTable.insertBatch(SQL("insert into transaction(transactionHash, blockHash, timestamp) values (?, ?, ?)").batch(batchTxParams: _*))
      transactionParams.clear()

      val batchInParams: Seq[Seq[Any]] = inputParams.map(i => Seq(i._1, i._2))
      inputTable.insertBatch(SQL("insert into input(transactionHash, inputScript) values (?, ?)").batch(batchInParams: _*))
      inputParams.clear()

      val batchOutParams: Seq[Seq[Any]] = outputParams.map(i => Seq(i._1, i._2))
      outputTable.insertBatch(SQL("insert into output(transactionHash, outputScript) values (?, ?)").batch(batchOutParams: _*))
      outputParams.clear()
  }
}