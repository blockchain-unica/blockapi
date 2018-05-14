package tcs.examples.ethereum.mongo


import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.{EthereumInternalTransaction, EthereumSettings, EthereumTransaction}
import tcs.examples.ethereum.mongo.levensthein.Helpers._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._


object EthereumERC20Info {

  val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("EthereumTokens")
  val collection: MongoCollection[Document] = database.getCollection("EthereumTokens")

  def main(args: Array[String]): Unit = {

    blockchain.start(2101639).end(2101647).foreach(block => {

      if(block.height%100 == 0){
        println("Current Block " + block.height)
      }

      /**controllo le transazioni esterne**/
      block.txs.foreach(tx => {

        if(collection.find(equal("contractAddress", tx.to)).first().results().nonEmpty){
          incInputTransaction(tx)
        }

        if(collection.find(equal("contractAddress", tx.from)).first().results().nonEmpty){
          incOutputTransaction(tx)
        }

      })

      /**controllo le transazioni interne**/
      if (block.internalTransactions.nonEmpty){

        block.internalTransactions.foreach(itx => {

          if(collection.find(equal("contractAddress", itx.to)).first().results().nonEmpty){
            incInputInternalTransaction(itx)
          }

          if(collection.find(equal("contractAddress", itx.from)).first().results().nonEmpty){
            incOutputInternalTransaction(itx)
          }

        })

      } else {
        println("No internal transaction")  //testing code
      }

    })

  }

  def incInputTransaction (tx : EthereumTransaction) : Unit = {
    incTxIn(tx.to, tx.value)
  }

  def incOutputTransaction (tx : EthereumTransaction) : Unit = {
    incTxOut(tx.from, tx.value)
  }

  def incInputInternalTransaction (itx : EthereumInternalTransaction) : Unit = {
    incTxIn(itx.to, itx.value)
  }

  def incOutputInternalTransaction (itx : EthereumInternalTransaction) : Unit = {
    incTxOut(itx.from, itx.value)
  }

  /**
    * This function increments a number of input transaction receive from a contract
    * @param address is the address of contract
    */
  def incTxIn (address :String, value :BigInt) : Unit ={

    if(collection.find(and(equal("contractAddress", address), exists("txIn"))).results().nonEmpty)
    {
      collection.findOneAndUpdate(and(equal("contractAddress", address), exists("txIn")),inc("txIn",1)).results()

    }else{
      collection.findOneAndUpdate(equal("contractAddress", address), set("txIn",1)).results()

    }
    //Incremento il bilancio. Fare la conversione perchè mongo da eccezione con i bigint
    if(collection.find(and(equal("contractAddress", address), exists("bilance"))).results().nonEmpty)
    {
      collection.findOneAndUpdate(and(equal("contractAddress", address), exists("bilance")),inc("bilance",value.toFloat)).results()

    }else{
      collection.findOneAndUpdate(equal("contractAddress", address), set("bilance",value.toFloat)).results()
    }
  }

  /**
    * This function increment the number of withdrawals from a contract
    * @param address
    */
  def incTxOut (address :String, value : BigInt) : Unit = {

    if(collection.find(and(equal("contractAddress", address), exists("txOut"))).results().nonEmpty)
    {
      collection.findOneAndUpdate(and(equal("contractAddress", address), exists("txOut")),inc("txOut",1)).results()

    }else{
      collection.findOneAndUpdate(equal("contractAddress", address), set("txOut",1)).results()

    }
    //decremento il bilancio
    if(collection.find(and(equal("contractAddress", address), exists("bilance"))).results().nonEmpty) {
      collection.findOneAndUpdate(and(equal("contractAddress", address), exists("bilance")), inc("bilance", - value.toFloat)).results()
    }

  }

}