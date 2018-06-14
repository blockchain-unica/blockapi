package tcs.examples.litecoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.litecoin.{LitecoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection


object MyBlockchainLite {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val myBlockchain = new Collection("myBlockchainLite", mongo)

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
