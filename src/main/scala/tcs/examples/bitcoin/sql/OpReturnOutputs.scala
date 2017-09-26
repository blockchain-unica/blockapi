package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.bitcoin.metadata.MetadataParser
import tcs.db.{DatabaseSettings, MySQL}
import tcs.db.mysql.Table
import tcs.utils.DateConverter.convertDate

/**
  * Created by Livio on 13/09/2017.
  */
object OpReturnOutputs {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("opreturn", MySQL, "user", "password")


    val startTimestamp = (System.currentTimeMillis / 1000)

    val outputTable = new Table(sql"""
      create table if not exists opreturnoutput(
        id serial not null primary key,
        transactionHash varchar(256) not null,
        txdate TIMESTAMP not null,
        protocol varchar(64) not null,
        metadata text not null
    )""",
    sql"""insert into opreturnoutput (transactionHash, txdate, protocol, metadata) values(?,?,?,?)""",
    mySQL)

    blockchain.start(290000).end(473100).foreach(block => {

      if(block.height % 10000 == 0) println("Block: " + block.height)

      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          if(out.isOpreturn()) {
            var protocol: String = MetadataParser.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.outScript.toString)
            var metadata: String = out.getMetadata()
            outputTable.insert(Seq(tx.hash.toString, convertDate(block.date), protocol, metadata))
          }
        })
      })
    })

    outputTable.close

    println("Total time: " + ((System.currentTimeMillis / 1000) - startTimestamp))
  }
}