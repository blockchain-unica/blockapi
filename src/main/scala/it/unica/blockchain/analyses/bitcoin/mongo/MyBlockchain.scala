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

    // 1) Connect to a blockchain client (Bitcoin Core)
    val blockchain = BlockchainLib.getBitcoinBlockchain(
      new BitcoinSettings("user", "password", "8332", MainNet))

    // 2) Connect to a DBMS and create a view (MongoDB collection)
    val mongo = new DatabaseSettings("myDatabase")
    val myBlockchain = new Collection("myBlockchain", mongo)

    // 3) Visit the blockchain and append values to the view
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
