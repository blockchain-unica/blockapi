package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

/**
  * Created by Stefano on 03/07/2017.
  */
object Addresses {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("bitcoin", "password", "https", "443", "co2.unica.it", "bitcoin-mainnet", MainNet))
    //val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))

    val mongo = new DatabaseSettings("clustering")

    val addresses = new Collection("addresses", mongo)

    blockchain.foreach(block => {

      println(block.height)

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