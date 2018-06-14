package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

/**
  * Same example of Addresses, but connects to a remote bitcoind instance (co2.unica.it)
  */
object AddressesWithRemoteHost {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("bitcoin", "passwordHere", "8332", "co2.unica.it", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val addresses = new Collection("addresses", mongo)

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
