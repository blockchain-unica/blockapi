package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.Tag
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 19/06/2017.
  */
object AddressesWithTags {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new MongoSettings("myDatabase")

    val outWithTags = new Collection("outWithTags", mongo)
    val tags = new Tag("src/main/scala/tcs/custom/input.txt")

    blockchain.foreach(block => {

      if(block.height % 1000 == 0){
        println(block.height)
      }

      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          out.getAddress(MainNet) match {
            case Some(add) =>
              tags.getValue(add) match {
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
  }
}
