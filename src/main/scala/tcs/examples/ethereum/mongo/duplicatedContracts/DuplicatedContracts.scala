package tcs.examples.ethereum.mongo.duplicatedContracts

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Accumulators._
import tcs.examples.ethereum.mongo.levensthein.Helpers._


object DuplicatedContracts {

  def main(args: Array[String]): Unit = {

    val mongo1: MongoClient = MongoClient()
    val db: MongoDatabase = mongo1.getDatabase("ethereum")
    val collection: MongoCollection[Document] = db.getCollection("contracts")

    collection.aggregate(Seq(group("$sourceCode",
      push("name","$contractName"),push("address","$contractAddress"),push("date","$date")),
      out("duplicatedContracts"))).results()

  }

}
