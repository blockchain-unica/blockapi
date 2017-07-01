package tcs.mongo

import java.util.Date

import org.mongodb.scala.{Completed, MongoClient, Observer}
import org.mongodb.scala.bson._
import tcs.blockchain.bitcoin.{BitcoinInput, BitcoinOutput}

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


    //TODO: handle lists
    val doc = list.map(convertPair(_)).reduce((a, b) => a ++ b)

    collection.insertOne(doc).subscribe(new Observer[Completed] {

      override def onNext(result: Completed): Unit = {}

      override def onError(e: Throwable): Unit = {}

      override def onComplete(): Unit = {}

    })

    val d = Document("redeemedTxHash" -> 1)
  }

  def convertPair(e: (String, Any)): Document = {
    e._2 match {

      case x: Boolean => Document(e._1 -> x)
      case x: Int => Document(e._1 -> x)
      case x: Long => Document(e._1 -> x)
      case x: Double => Document(e._1 -> x)
      case x: Array[Byte] => Document(e._1 -> x)
      case x: Date => Document(e._1 -> x)
      case None => Document(e._1 -> None)
      case x: Seq[Any] => Document(e._1 -> x.map(i => Seq(convert(i))).reduce(_ ++ _))
      case x: Any => Document(e._1 -> x.toString)
    }
  }

  def convert(e: Any) = {
    e match {
      case x: BitcoinInput =>
        val inHash = x.redeemedTxHash
        Document("redeemedTxHash" -> (if (inHash != null) inHash.toString() else ""),
          "redeemedOutIndex" -> x.redeemedOutIndex,
          "value" -> x.value,
          "inScript" -> x.inScript.toString,
          "isCoinBase" -> x.isCoinbase)
      case x: BitcoinOutput =>
        Document("index" -> x.index,
          "value" -> x.value,
          "outScript" -> x.outScript.toString)
      case x: Any => Document(x.toString)
    }
  }

  def close = {
    mongoClient.close()
  }
}


