package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Stefano on 03/07/2017.
  */
object Addresses {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("clustering")

    val addresses = new Collection("addresses", mongo)

    blockchain.foreach(block => {
      if(block.height % 1000 == 0){
        println(block.height)
      }

      block.bitcoinTxs.foreach(tx => {

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