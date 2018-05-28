package tcs.examples.litecoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.litecoin.{LitecoinSettings, MainNet}
import tcs.externaldata.metadata.MetadataParser
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, MySQL}
import tcs.utils.converter.DateConverter.convertDate

/**
  * Created by Giulia on 15/05/2018.
  */
object OpReturnOutputsLite {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9332", MainNet))
    val mySQL = new DatabaseSettings("opreturn", MySQL, "user", "password")


    val startTime = System.currentTimeMillis() / 1000

    val outputTable = new Table(
      sql"""
      create table if not exists opreturnoutputlite(
        id serial not null primary key,
        transactionHash varchar(256) not null,
        txdate TIMESTAMP not null,
        protocol varchar(64) not null,
        metadata text not null
    )""",
      sql"""insert into opreturnoutputlite (transactionHash, txdate, protocol, metadata) values(?,?,?,?)""",
      mySQL)

    blockchain.start(500000).end(1200000).foreach(block => {

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