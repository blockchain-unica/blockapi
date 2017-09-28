package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.{DatabaseSettings, MySQL}
import tcs.db.mysql.Table
import tcs.custom.Tag
import tcs.utils.DateConverter.convertDate


/**
  * Created by Livio on 14/09/2017.
  */
object AddressesWithTags {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("outwithtags", MySQL, "user", "password")
    val tags = new Tag("src/main/scala/tcs/custom/bitcoin/tagsList.txt")

    val startTime = System.currentTimeMillis() / 1000

    val outTable = new Table(
      sql"""
        create table if not exists tagsoutputs(
          id serial not null primary key,
          transactionHash varchar(256) not null,
          txdate TIMESTAMP not null,
          outvalue bigint unsigned,
          address varchar(256),
          tag varchar(256)
       )""",
      sql"""insert into tagsoutputs (transactionHash, txdate, outvalue, address, tag) values (?, ?, ?, ?, ?)""",
      mySQL)

    blockchain.end(473100).foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          out.getAddress(MainNet) match {
            case Some(add) =>
              tags.getValue(add) match {
                case Some(tag) => {
                  outTable.insert(Seq(tx.hash.toString, convertDate(block.date), out.value, add.toString, tags.getValue(add)))
                }
                case None =>
              }
            case None =>
          }
        })
      })
    })

    outTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
