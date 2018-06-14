package it.unica.blockchain.db.mongo

import java.util.Date

import org.mongodb.scala.bson._
import shapeless.TypeCase
import it.unica.blockchain.blockchains.bitcoin.{BitcoinInput, BitcoinOutput}

/**
  * Created by Livio on 11/09/2017.
  */

object BsonConverter {

  private val pairsList = TypeCase[List[(String, Any)]]


  def convertPair(e: (String, Any)): Document = {


    e._2 match {
      case pairsList(x) => Document(e._1 -> x.map(i => convertPair(i)).reduce(_ ++ _))
      case None => Document(e._1 -> "")
      case x: Seq[Any] => Document(e._1 -> x.map(i => Seq(convert(i))).reduce(_ ++ _))
      case x: Any => Document(e._1 -> convert(x))
    }
  }

  def convert(e: Any): BsonValue = {
    e match {
      case x: Boolean => toBson(x)
      case x: Int => toBson(x)
      case x: Long => toBson(x)
      case x: Double => toBson(x)
      case x: Array[Byte] => toBson(x)
      case x: Date => toBson(x)
      case x: BitcoinInput =>
        val inHash = x.redeemedTxHash
        Document("redeemedTxHash" -> (if (inHash != null) inHash.toString() else ""),
          "redeemedOutIndex" -> x.redeemedOutIndex,
          "value" -> x.value,
          "inScript" -> x.inScript.toString,
          "isCoinBase" -> x.isCoinbase).toBsonDocument
      case x: BitcoinOutput =>
        Document("index" -> x.index,
          "value" -> x.value,
          "outScript" -> x.outScript.toString).toBsonDocument
      case pairsList(x) => toBson(x.map(i => convertPair(i)).reduce(_ ++ _))
      case x: Seq[Any] => toBson(x.map(i => Seq(convert(i))).reduce(_ ++ _))
      case x: Document => toBson(x)
      case x: Any => toBson(x.toString)
    }
  }

  def toBson[T](v: T)(implicit transformer: BsonTransformer[T]): BsonValue = {
    transformer(v)
  }


  def toAscii(hex: String) = {
    require(hex.size % 2 == 0,
      "Hex must have an even number of characters. You had " + hex.size)
    val sb = new StringBuilder
    for (i <- 0 until hex.size by 2) {
      val str = hex.substring(i, i + 2)
      sb.append(Integer.parseInt(str, 16).toChar)
    }
    sb.toString
  }
}