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

    var doc: Document = Document()

    doc = list.map(a => {
      a._2 match {
        case l: List[Any] => (a._1, if (l.isEmpty) "Empty List" else l)
        case _ => (a._1, a._2)
      }
    }).map(Convert.convertPair).reduce(_ ++ _)
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

  def append(x : Document) = collection.insertOne(x).subscribe(new Observer[Completed] {

    override def onNext(result: Completed): Unit = {}

    override def onError(e: Throwable): Unit = {}

    override def onComplete(): Unit = {}

  })
}


