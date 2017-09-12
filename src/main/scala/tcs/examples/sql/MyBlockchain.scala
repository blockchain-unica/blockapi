package tcs.examples.sql

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
    val mySQL = new DatabaseSettings("myBlockchain", MySQL, "user", "password")

    val transactionTable = new Table(sql"""
      create table transaction (
        transactionHash varchar(256) not null primary key,
        blockHash varchar(256) not null,
        timestamp TIMESTAMP not null
      )""", mySQL)

    val inputTable = new Table(sql"""
      create table input (
        id serial not null primary key,
        transactionHash varchar(256) not null,
        inputScript varchar(40000) not null
      )""", mySQL)

    val outputTable = new Table(sql"""
      create table output (
        id serial not null primary key,
        transactionHash varchar(256) not null,
        outputScript varchar(40000) not null
      )""", mySQL)

    blockchain.end(100000).foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        println(tx.hash)
        transactionTable.insert(sql"insert into transaction (transactionHash, blockHash, timestamp) values (${tx.hash}, ${block.hash}, ${block.date})")

        tx.inputs.foreach(in => {
          inputTable.insert(sql"insert into input (transactionHash, inputScript) values (${tx.hash}, ${in.inScript.toString})")
        })

        tx.outputs.foreach(out => {
          outputTable.insert(sql"insert into output (transactionHash, outputScript) values (${tx.hash}, ${out.outScript.toString})")
        })
      })
    })

    transactionTable.close
    inputTable.close
    outputTable.close
  }
}