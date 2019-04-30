package it.unica.blockchain.analyses.litecoin.sql
/*
import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.externaldata.metadata.MetadataParser
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.utils.converter.DateConverter.convertDate

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

    //OP_RETURN has been included in Litecoin since 0.9 release
    //Blocks before 2014 will return null on every row
    
    blockchain.start(500000).foreach(block => {

      if (block.height % 10000 == 0) println("Block: " + block.height)

      block.txs.foreach(tx => {
        tx.outputs.foreach(out => {
          if (out.isOpreturn()) {
            var protocol: String =
              MetadataParser.getApplication(tx.inputs.head.outPoint.toString.substring(0, 64), out.transOut.toString)
            var metadata: String =
              out.getMetadata()
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
*/