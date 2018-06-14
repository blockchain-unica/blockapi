package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

/**
  * Created by stefano on 13/06/17.
  */
object MyBlockchain {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val myBlockchain = new Collection("myBlockchain", mongo)

    blockchain.foreach(block => {
      block.txs.foreach(tx => {
        myBlockchain.append(List(
          ("txHash", tx.hash),
          ("blockHash", block.hash),
          ("date", block.date),
          ("inputs", tx.inputs),
          ("outputs", tx.outputs)
        ))
      })
    })

    myBlockchain.close
  }
}
