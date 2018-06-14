package tcs.examples.litecoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.litecoin.{LitecoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection


object Addresses {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9332", MainNet))
    val mongo = new DatabaseSettings("clustering")

    val addresses = new Collection("addresseslite", mongo)

    blockchain.foreach(block => {
      block.txs.foreach(tx => {
        addresses.append(List(
          ("h", tx.hash),
          ("d", block.date.getTime),
          ("i", tx.inputs.flatMap(in => Seq(in.getAddress(MainNet).getOrElse("").toString))),
          ("o", tx.outputs.flatMap(out => Seq(out.getAddress(MainNet).getOrElse("").toString)))
        ))
      })
    })

    addresses.close
  }
}