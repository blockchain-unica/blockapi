package tcs.mongo

import java.util.Date

import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson._

/**
  * Created by stefano on 13/06/17.
  */
class Collection(val name: String, val settings: MongoSettings) {

  private val mongoClient: MongoClient =
    if (settings.user.eq(""))
      MongoClient("mongodb://" + settings.host + ":" + settings.port)
    else
      MongoClient("mongodb://" + settings.user + ":" + settings.psw + "@" + settings.host + ":" + settings.port)

  private val database = mongoClient.getDatabase(settings.database)
  private val collection = database.getCollection(name)

  def append(list: List[(String, Any)]): Unit = {

    val doc = list.map(e => e._2 match {
      case x: Boolean => Document(e._1 -> x)
      case x: Int => Document(e._1 -> x)
      case x: Long => Document(e._1 -> x)
      case x: Double => Document(e._1 -> x)
      case x: Array[Byte] => Document(e._1 -> x)
      case x: Date => Document(e._1 -> x)
      case None => Document(e._1 -> None)
      case x: Any => Document(e._1 -> x.toString)
    }).reduce((a, b) => a ++ b)

    collection.insertOne(doc)

  }
}
