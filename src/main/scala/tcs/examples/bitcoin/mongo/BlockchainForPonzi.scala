package tcs.examples.bitcoin.mongo

import org.mongodb.scala.bson.collection.immutable.Document
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.bitcoin.Exchange
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object BlockchainForPonzi {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("sergio")

    val myBlockchain = new Collection("transaction", mongo)

    blockchain.start(481820).foreach(block => {

      if (block.height % 1 == 0) println("Block: " + block.height)

      block.bitcoinTxs.foreach(tx => {
        myBlockchain.append(Document(
          "txid" -> tx.hash.toString,
          "blockhash" -> block.hash.toString,
          "time" -> block.date.getTime,

          if (tx.inputs.head.redeemedOutIndex != -1) {
            "vin" -> tx.inputs.map(i =>
              Document("txid" -> i.redeemedTxHash.toString,
                "vout" -> i.redeemedOutIndex,
                "address" -> i.getAddress(MainNet).getOrElse("").toString))
          }
          else "vin" -> Document("coinbase" -> true),

          "vout" -> tx.outputs.map(o =>
            Document("value" -> o.value,
              "addresses" ->
                List(o.getAddress(MainNet).getOrElse("").toString)
            ))
          ,
          "blockheight" -> block.height
          ,
          "rate" -> Exchange.getRate(block.date)
        ))

      })
    })

    myBlockchain.close
  }
}
