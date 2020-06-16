package it.unica.blockchain.analyses.bitcoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.{DatabaseSettings, Mongo}
import it.unica.blockchain.mongo.Collection

/**
  * Created by stefano on 13/06/17.
  */
object BitcoinAbuseTransactions {
  def main(args: Array[String]): Unit = {

    // 1) Connect to a blockchain client (Bitcoin Core)
    val blockchain = BlockchainLib.getBitcoinBlockchain(
      new BitcoinSettings("user", "password", "8332", "1.1.1.1", MainNet))

    // 2) Connect to a DBMS and create a view (MongoDB collection)
    val mongo = new DatabaseSettings("myDatabase")
    val myBlockchain = new Collection("myBlockchain", mongo)

    // load set of addresses

    val bufferedSource = io.Source.fromFile("BitcoinAbuseDataset.csv")

    val addressesList = bufferedSource.getLines.map(line => line.split(",").map(_.trim).apply(0))

    val addressesSet = addressesList.toSet


    // 3) Visit the blockchain and append values to the view
    blockchain.foreach(block => {
      block.txs.foreach(tx => {

        //map each input to a boolean value, which depends whether the input address is in the bitcoinabuse dataset
        //reduce the boolean elements with the OR function, so the result is the disjunction of all values
        //the result is true if at least one input in the bitcoinabuse dataset
        val inputContains = tx.inputs.map( in => addressesSet contains in.getAddress(MainNet).get.toBase58).reduce(_||_)

        val outputContains = tx.inputs.map( in => addressesSet contains in.getAddress(MainNet).get.toBase58).reduce(_||_)

        if (inputContains || outputContains ){
          myBlockchain.append(List(
            ("txHash", tx.hash),
            ("blockHash", block.hash),
            ("date", block.date),
            ("inputs", tx.inputs),
            ("outputs", tx.outputs)
          ))
        }

      })
    })

    myBlockchain.close
  }
}
