package it.unica.blockchain.examples.ethereum.mongo.duplicatedContracts

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Accumulators._
import it.unica.blockchain.examples.ethereum.mongo.levensthein.Helpers._

/**
  *  The script identifies duplicated contracts,
  *  i.e. contracts that have exactly the same source code as another contract in the blockchain.
  *
  *  Please start MongoDB with the following command to execute correctly the query:
  *  mongod --setParameter failIndexKeyTooLong=false
  *
  *  @author Flavia Murru
  *  @author Francesca Malloci
  *  @author Fabio Carta
  *
  */

object DuplicatedContracts {

  def main(args: Array[String]): Unit = {

    val mongo1: MongoClient = MongoClient()
    val db: MongoDatabase = mongo1.getDatabase("ethereum") //creates the DB mongoDB
    val collection: MongoCollection[Document] = db.getCollection("contracts") //creates the collection contracts

    /**
      * Stores into the collection duplicatedContracts all contracts that have a duplicate or not
      * For each contract that has a duplicate, a list is created
      */
    collection.aggregate(Seq(group("$sourceCode",
      push("name","$contractName"),push("address","$contractAddress"),push("date","$date")),
      out("duplicatedContracts"))).results()

  }

}
