package tcs.mongo

import org.mongodb.scala.bson._
import org.mongodb.scala.{Completed, MongoClient, Observer}
import tcs.db.DatabaseSettings
import tcs.utils.Convert

/**
  * Created by stefano on 13/06/17.
  */
class Collection(val name: String, val settings: DatabaseSettings) {

  private val mongoClient: MongoClient =
    if (settings.user.eq(""))
      MongoClient("mongodb://" + settings.host + ":" + settings.port)
    else
      MongoClient("mongodb://" + settings.user + ":" + settings.psw + "@" + settings.host + ":" + settings.port)

  private val database = mongoClient.getDatabase(settings.database)
  private val collection = database.getCollection(name)

  def append(list: List[(String, Any)]): Unit = {


    //TODO: handle lists
    val doc = list.map(Convert.convertPair(_)).reduce((a, b) => a ++ b)

    collection.insertOne(doc).subscribe(new Observer[Completed] {

      override def onNext(result: Completed): Unit = {}

      override def onError(e: Throwable): Unit = {}

      override def onComplete(): Unit = {}

    })

    val d = Document("redeemedTxHash" -> 1)
  }

  def close = {
    mongoClient.close()
  }
}


