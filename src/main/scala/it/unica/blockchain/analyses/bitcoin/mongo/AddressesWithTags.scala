package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.custom.Tag
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

import scala.collection.mutable

/**
  * Created by Livio on 19/06/2017.
  */
object AddressesWithTags {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("myDatabase")

    val outWithTags = new Collection("outWithTags", mongo)
    var tags = Tag.getTagsFromFile("src\\main\\scala\\it\\unica\\blockchain\\externaldata\\tags\\tagsList.txt")

    blockchain.foreach(block => {

      if(block.height % 10000 == 0){
        println(block.height)
      }

      block.txs.foreach(tx => {
        tx.outputs.foreach(out => {
          out.getAddress(MainNet) match {
            case Some(add) =>
              tags.get(add.toString) match {
                case Some(tag) =>
                  outWithTags.append(List(
                    ("txHash", tx.hash),
                    ("date", block.date),
                    ("value", out.value),
                    ("address", add),
                    ("tags", tag)
                  ))
                case None =>
              }
            case None =>
          }
        })
      })
    })

    outWithTags.close
  }
}
