package it.unica.blockchain.examples.bitcoin.sql

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.utils.converter.DateConverter.convertDate
import it.unica.blockchain.utils.converter.DateConverter
import scalaj.http.Http
import play.api.libs.json.Json
import java.util.Date

object CrossValidationBitcoin {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("validation", MySQL, "user", "password")

    val startTime = System.currentTimeMillis() / 1000

    val txTool = new Table(
      sql"""
      create table if not exists txtool(
        id serial not null primary key,
        txHash varchar(256) not null,
        txdate TIMESTAMP not null,
        numinput int,
        numoutput int,
        outputsum bigint
    )""",
      sql"""insert into txtool (txHash, txdate, numinput, numoutput, outputsum) values(?,?,?,?,?)""",
      mySQL)

    val txExp = new Table(
      sql"""
      create table if not exists txexp(
        id serial not null primary key,
        txHash varchar(256) not null,
        txdate TIMESTAMP not null,
        numinput int,
        numoutput int,
        outputsum bigint
    )""",
      sql"""insert into txexp (txHash, txdate, numinput, numoutput, outputsum) values(?,?,?,?,?)""",
      mySQL)

    blockchain.start(200000).end(200001).foreach(block => {

      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(f = tx => {

        txTool.insert(Seq(
          tx.hash.toString,
          convertDate(block.date),
          tx.inputs.length,
          tx.outputs.length,
          tx.getOutputsSum()))

        val jsonString = Http("https://blockchain.info/it/rawtx/" + tx.hash).timeout(1000000000, 1000000000).asString.body
        val jsonObject = Json.parse(jsonString)
        val in = (jsonObject \ "inputs" \\ "sequence")
        val out = (jsonObject \ "out" \\ "value")
        val sum = out.map(_.as[Long]).sum
        val t = new Date (((jsonObject \ "time").get.as[Long]) *1000)

        txExp.insert(Seq(
          (jsonObject \ "hash").as[String],
          convertDate(t),
          in.length,
          out.length,
          sum))

      })
    })

    txTool.close
    txExp.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
