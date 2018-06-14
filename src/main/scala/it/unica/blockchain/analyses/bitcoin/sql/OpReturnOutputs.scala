package it.unica.blockchain.analyses.bitcoin.sql

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.utils.converter.DateConverter.convertDate
import it.unica.blockchain.externaldata.metadata.MetadataParser

/**
  * Created by Livio on 13/09/2017.
  */
object OpReturnOutputs {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("opreturn", MySQL, "user", "password")


    val startTime = System.currentTimeMillis() / 1000

    val outputTable = new Table(
      sql"""
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

      if (block.height % 10000 == 0) println("Block: " + block.height)

      block.txs.foreach(tx => {
        tx.outputs.foreach(out => {
          if (out.isOpreturn()) {
            var protocol: String = MetadataParser.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.transOut.toString)
            var metadata: String = out.getMetadata()
            outputTable.insert(Seq(tx.hash.toString, convertDate(block.date), protocol, metadata))
          }
        })
      })
    })

    outputTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}