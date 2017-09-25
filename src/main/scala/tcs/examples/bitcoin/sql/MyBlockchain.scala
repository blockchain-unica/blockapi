package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.mysql.Table
import tcs.db.{DatabaseSettings, MySQL}

/**
  * Created by Livio on 14/06/2017.
  */
object MyBlockchain{
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("myblockchain", MySQL, "user", "password")

    val txTable = new Table(sql"""
      create table if not exists transaction(
        txid int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        blockHash varchar(256) not null,
        timestamp varchar(40) not null
      ) """,
      sql"""insert into transaction(transactionHash, blockHash, timestamp) values (?, ?, ?)""",
      mySQL)

    val inTable = new Table(sql"""
      create table if not exists input(
        id int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        inputScript text not null
      ) """,
      sql"""insert into input(transactionHash, inputScript) values (?, ?)""",
      mySQL)

    val outTable = new Table(sql"""
      create table if not exists output(
        id int(10) unsigned auto_increment not null primary key,
        transactionHash varchar(256) not null,
        outputScript text not null
      ) """,
      sql"""insert into output(transactionHash, outputScript) values (?, ?)""",
      mySQL)


    blockchain.end(473100).foreach(block => {
      block.bitcoinTxs.foreach(tx => {

        txTable.insert(Seq(tx.hash.toString, block.hash.toString, block.date))

        tx.inputs.foreach(in => { inTable.insert(Seq(tx.hash.toString, in.inScript.toString)) })

        tx.outputs.foreach(out => { outTable.insert(Seq(tx.hash.toString, out.outScript.toString)) })
      })
    })

    // Write last values
    txTable.flush
    inTable.flush
    outTable.flush
  }
}