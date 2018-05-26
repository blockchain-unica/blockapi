package tcs.examples.ethereum.mongo

import org.apache.commons.lang3.StringUtils

import scala.collection.immutable.HashSet
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.{EthereumInternalTransaction, EthereumSettings, EthereumTransaction}
import tcs.examples.ethereum.mongo.levensthein.Helpers._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._



/** For each contract, if exists, this script adds to database:
  * - balance
  * - number of transaction in
  * - number of transaction out
  *
  *
  * @author Chessa Stefano Raimondo
  * @author Guria Marco
  * @author Manai Alessio
  * @author Speroni Alessio
  * */

object EthereumERC20Info {

  val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("EthereumTokens")
  val collection: MongoCollection[Document] = database.getCollection("EthereumTokens")


  def main(args: Array[String]): Unit = {

    var addressSet : HashSet[String] = HashSet()

    /**it moves contract from collection to hashset*/
    for(x <- collection.find().projection(
      fields(include("contractAddress"), excludeId())).results()){
        addressSet += StringUtils.substringBetween(x.toString(), "value='","'}))")
      }

    println(addressSet.size)

    blockchain.start(2101639).end(2101647).foreach(block => {

      if(block.height%100 == 0){
        println("Current Block " + block.height)
      }

      /** for each transaction, check if exist an input or output transaction on a contract
        * if exist, it updates balance and transactions into db
        */

      block.txs.foreach(tx => {

        if (addressSet.contains(tx.to)){
          incInputTransaction(tx)
        }

        if (addressSet.contains(tx.from)){
          incOutputTransaction(tx)
        }

      })

      /** for each internal transaction, check if exist an input or output transaction on a contract
        * if exist, it updates balance and transactions into db
        */

      if (block.internalTransactions.nonEmpty){

        block.internalTransactions.foreach(itx => {

          if (addressSet.contains(itx.to)){
            incInputInternalTransaction(itx)
          }

          if (addressSet.contains(itx.from)){
            incOutputInternalTransaction(itx)
          }

        })

      }

    })

  }

  /***this function increments number of transaction in by one, checking transactions*/
  def incInputTransaction (tx : EthereumTransaction) : Unit = {
    incTxIn(tx.to, tx.value)
  }

  /***increments number of transaction out by one, checking transactions*/
  def incOutputTransaction (tx : EthereumTransaction) : Unit = {
    incTxOut(tx.from, tx.value)
  }

  /***increments number of transaction in by one, checking internal transactions*/
  def incInputInternalTransaction (itx : EthereumInternalTransaction) : Unit = {
    incTxIn(itx.to, itx.value)
  }

  /***it increments number of transaction out by one, checking internal transactions*/
  def incOutputInternalTransaction (itx : EthereumInternalTransaction) : Unit = {
    incTxOut(itx.from, itx.value)
  }

  /**
    * This function increments a number of input transaction receive from a contract
    * Also it updates the balance value of contract
    * @param address is the address of contract
    */
  def incTxIn (address :String, value :BigInt) : Unit ={

    if(collection.find(and(equal("contractAddress", address),
      exists("txIn"))).results().nonEmpty) {

      collection.findOneAndUpdate(and(equal("contractAddress", address),
        exists("txIn")),inc("txIn",1)).results()

    } else {

      collection.findOneAndUpdate(equal("contractAddress", address), set("txIn",1)).results()

    }

    //balance increment
    if(collection.find(and(equal("contractAddress", address),
      exists("balance"))).results().nonEmpty) {

      collection.findOneAndUpdate(and(equal("contractAddress", address),
        exists("balance")), inc("balance", value.toFloat)).results()

    } else {

      collection.findOneAndUpdate(equal("contractAddress", address),
        set("balance", value.toFloat)).results()
    }
  }

  /**
    * This function increment the number of withdrawals from a contract
    * Also it updates the balance value of contract
    * @param address
    */
  def incTxOut (address :String, value : BigInt) : Unit = {

    if(collection.find(and(equal("contractAddress", address),
      exists("txOut"))).results().nonEmpty) {

      collection.findOneAndUpdate(and(equal("contractAddress", address),
        exists("txOut")),inc("txOut",1)).results()

    } else {
      collection.findOneAndUpdate(equal("contractAddress", address), set("txOut",1)).results()
    }

    //balance decrement
    if(collection.find(and(equal("contractAddress", address),
      exists("balance"))).results().nonEmpty) {

      collection.findOneAndUpdate(and(equal("contractAddress", address),
        exists("balance")), inc("balance", - value.toFloat)).results()
    }

 }

}