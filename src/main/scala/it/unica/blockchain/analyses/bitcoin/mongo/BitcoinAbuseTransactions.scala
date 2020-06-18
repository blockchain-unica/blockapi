package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, HasAddress, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, Mongo}
import it.unica.blockchain.mongo.Collection

import scala.collection.immutable.HashSet

/**
  * Created by stefano on 17/06/20.
  */
object BitcoinAbuseTransactions {
  def main(args: Array[String]): Unit = {

    // 1) Connect to a blockchain client (Bitcoin Core)
    val blockchain = BlockchainLib.getBitcoinBlockchain(
      new BitcoinSettings("user", "password", "8332", MainNet))

    // 2) Connect to a DBMS and create a view (MongoDB collection)
    val mongo = new DatabaseSettings("myDatabase", Mongo)
    val myBlockchain = new Collection("myBlockchain", mongo)

    // load set of addresses

    val bufferedSource = io.Source.fromFile("BitcoinAbuseDataset.csv")

    val addressesList = bufferedSource.getLines.map(line => line.split(",").map(_.trim).apply(0))

    bufferedSource.close()

    val addressesSet = HashSet() ++ addressesList

    val startTime = System.currentTimeMillis() / 1000

    // 3) Visit the blockchain and append values to the view
    var i = 0;

    blockchain.start(352966).foreach(block => {
      block.txs.foreach(tx => {

        //map each input to a boolean value, which depends whether the input address is in the bitcoinabuse dataset
        //reduce the boolean elements with the OR function, so the result is the disjunction of all values
        //the result is true if at least one input in the bitcoinabuse dataset

        val inputContains = tx.inputs.map(isAddrInSet(_, addressesSet)).reduce(_ || _)

        val outputContains = tx.outputs.map(isAddrInSet(_, addressesSet)).reduce(_ || _)

        if (inputContains || outputContains ){
          myBlockchain.append(List(
            "txid" -> tx.hash.toString,
            "time" -> block.date.getTime,

            if (tx.inputs.head.redeemedOutIndex != -1) {
              "vin" -> tx.inputs.map(i =>
                List("txid" -> i.redeemedTxHash.toString,
                  "vout" -> i.redeemedOutIndex,
                  "address" -> getAddrOrEmptyString(i)))
            }
            else "vin" -> List("coinbase" -> true),

            "vout" -> tx.outputs.map(o =>
              List("value" -> o.value,
                "address" -> getAddrOrEmptyString(o)
              ))
          ))
        }
      })

      if (i % 10000 == 0) {
        println("Block: " + i)
      }
      i += 1
    })

    myBlockchain.close
    println("Done")

    val totalTime = System.currentTimeMillis() / 1000 - startTime

    println("Total time: " + totalTime)
  }

  private def isAddrInSet(x: HasAddress, set: Set[String]): Boolean = {
    val stringAddr = getAddrOrEmptyString(x)

    if (stringAddr.equals(""))
      false
    else
      set contains stringAddr
  }

  private def getAddrOrEmptyString(x: HasAddress): String =
    x.getAddress(MainNet) match {
      case None => ""
      case Some(address) => address.toBase58
    }


}
