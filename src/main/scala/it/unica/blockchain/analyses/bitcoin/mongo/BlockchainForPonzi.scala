package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.externaldata.rates.BitcoinRates
import it.unica.blockchain.mongo.Collection

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object BlockchainForPonzi {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("blockchain")

    val myBlockchain = new Collection("transaction", mongo)

    val startTime = System.currentTimeMillis() / 1000


    blockchain.start(492575).end(515000).foreach(block => {

      if (block.height % 10000 == 0) println("Block: " + block.height)

      block.txs.foreach(tx => {

        myBlockchain.append(List(
          "txid" -> tx.hash.toString,
          "time" -> block.date.getTime,

          if (tx.inputs.head.redeemedOutIndex != -1) {
            "vin" -> tx.inputs.map(i =>
              List("txid" -> i.redeemedTxHash.toString,
                "vout" -> i.redeemedOutIndex,
                "address" -> i.getAddress(MainNet).getOrElse("").toString))
          }
          else "vin" -> List("coinbase" -> true),

          "vout" -> tx.outputs.map(o =>
            List("value" -> o.value,
              "addresses" ->
                List(o.getAddress(MainNet).getOrElse("").toString)
            ))
          ,
          "rate" -> BitcoinRates.getRate(block.date)
        ))



      })
    })


    myBlockchain.close
    println("Done")

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
  }
}
