package it.unica.blockchain.examples.bitcoin.sql

import scalikejdbc._
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, MySQL}
import it.unica.blockchain.db.sql.Table
import it.unica.blockchain.custom.Tag
import it.unica.blockchain.utils.converter.DateConverter.convertDate


/**
  * Created by Livio on 14/09/2017.
  */
object AddressesWithTags {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mySQL = new DatabaseSettings("outwithtags", MySQL, "user", "password")
    val tags = Tag.getTagsFromFile("src/main/scala/tcs/custom/bitcoin/tagsList.txt")

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
      block.txs.foreach(tx => {
        tx.outputs.foreach(out => {
          out.getAddress(MainNet) match {
            case Some(add) =>
              tags.get(add.toString) match {
                case Some(tag) => {
                  outTable.insert(Seq(tx.hash.toString, convertDate(block.date), out.value, add.toString, tags.get(add.toString)))
                }
                case None =>
              }
            case None =>
          }
        })
      })

      if(block.height % 10000 == 0){
       println(block.height)
      }
    })

    outTable.close

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
    println("Computational time: " + (totalTime - Table.getWriteTime))
    println("Database time: " + Table.getWriteTime)
  }
}
