package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.externaldata.rates.BitcoinRates

object BlockchainForPonzi {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("blockchain")

    val myBlockchain = new Collection("transaction", mongo)

    blockchain.end(515000).foreach(block => {

      if (block.height % 10000 == 0) println("Block: " + block.height)

      block.txs.foreach(tx => {
        myBlockchain.append(List(
          "txid" -> tx.hash.toString,
          "blockhash" -> block.hash.toString,
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
          "blockheight" -> block.height
          ,
          "rate" -> BitcoinRates.getRate(block.date)
        ))

      })
    })

    myBlockchain.close
  }
}
