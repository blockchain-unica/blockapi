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

    blockchain.end(473100).foreach(block => {
      if(block.height%500==0) println(block.height)

      block.bitcoinTxs.foreach(tx => {
          transactionTable.insert(sql"insert into transaction (transactionHash, blockHash, timestamp) values (${tx.hash.toString}, ${block.hash.toString}, ${block.date})")

          tx.inputs.foreach(in => {
            inputTable.insert(sql"insert into input (transactionHash, inputScript) values (${tx.hash.toString}, ${in.inScript.toString})")
          })

          tx.outputs.foreach(out => {
            outputTable.insert(sql"insert into output (transactionHash, outputScript) values (${tx.hash.toString}, ${out.outScript.toString})")
          })
      })
    })

    transactionTable.close
    inputTable.close
    outputTable.close
  }
}