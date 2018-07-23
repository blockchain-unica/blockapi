package it.unica.blockchain.externaldata.metadata

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import org.mongodb.scala.Document
import play.api.libs.json.{JsArray, JsBoolean, JsObject, Json}
import scalaj.http.Http
import com.github.tototoshi.csv.CSVReader
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.externaldata.metadata.Asset
import it.unica.blockchain.mongo.Collection
import org.bson.types.BasicBSONList
import org.mongodb.scala.Document
import org.mongodb.scala.bson._
import play.api.libs.json.{JsArray, JsObject, Json}


import scala.util.{Failure, Success, Try}

case class Asset(id: Long, block: Long, time: Long, asset: String, description: String, total: BigDecimal, divisible: Boolean, locked: Boolean)

object Asset {
  val AssetsCSVFileName = "queries/counterpartyTokens/assets.csv"

  private val XChainAssetsEndpoint = "https://xchain.io/explorer/assets"
  private val XChainHoldersEndpoint = "https://xchain.io/api/holders/"

  /**
    * Download assets and dump them to csv file
    */
  def getCounterpartyAssets(saveToFile: Boolean = true): Try[Seq[Asset]] = {
    val mbAssets = downloadAssets()

    if (saveToFile) {
      mbAssets match {
        case Success(assets) =>
          saveAssets(
            "queries/counterpartyTokens/assets.csv",
            assets)
        case Failure(exception) =>
          println(exception.getMessage)
      }
    }

    mbAssets
  }

  private def downloadAssets(): Try[Seq[Asset]] = {
    val response = Http(XChainAssetsEndpoint)
      .param("start", "0")
      .param("length", "73702")
      .param("action", "next")
      .param("offset", "")
      .execute(Json.parse)

    response.body("success") match {
      case JsBoolean(true) =>
        response.body("data") match {
          case JsArray(data) =>
            Success(for (asset <- data)
              yield Asset(
                asset.asInstanceOf[JsArray](0).as[Long],
                asset.asInstanceOf[JsArray](1).as[String].toLong,
                asset.asInstanceOf[JsArray](2).as[String].toLong,
                asset.asInstanceOf[JsArray](3).as[String],
                asset.asInstanceOf[JsArray](4).as[String],
                BigDecimal(asset.asInstanceOf[JsArray](5).as[String]),
                asset.asInstanceOf[JsArray](6).as[String].toBoolean,
                asset.asInstanceOf[JsArray](7).as[String].toBoolean
              ))
        }
      case _ =>
        Failure(new Exception("Error: unable to load assets from xchain.io."))
    }
  }

  private def saveAssets(file: String, assets: Seq[Asset]): Unit = {
    val writer = CSVWriter.open(file, append = false)

    writer.writeRow(List("id", "block", "time", "asset", "description", "total", "divisible", "locked"))
    for (asset <- assets) {
      writer.writeRow(List(asset.id, asset.block, asset.time, asset.asset, asset.description, asset.total, asset.divisible, asset.locked))
    }

    writer.close()
  }

  /**
    * Read assets, retreive holders info and dump them to mongo
    */
  def retrieveHoldersAndSaveCounterpartyAssets(): Unit = {
    val assets = loadAssets(Asset.AssetsCSVFileName)

    val assetsAndHolders = retrieveAssetsHolders(assets)

    val collection = new Collection("assetsAndHolders", new DatabaseSettings("myDatabase", user = "root", psw = "root"))
    saveAssetsAndHolders(assetsAndHolders,
      collection)
    collection.close
  }

  private def loadAssets(file: String): Seq[Asset] = {
    val reader = CSVReader.open(file)

    reader.all().tail.map(row => Asset(
      row(0).toLong,
      row(1).toLong,
      row(2).toLong,
      row(3),
      row(4),
      BigDecimal(row(5)),
      row(6).toBoolean,
      row(7).toBoolean
    ))
  }

  private def retrieveAssetsHolders(assets: Seq[Asset]): Seq[(Asset, Seq[(String, BigDecimal)])] = {
    assets.map(asset => {
      val resonse = Http(XChainHoldersEndpoint + asset.asset).execute(Json.parse)
      resonse.body("data") match {
        case JsArray(data) =>
          val holders = for (element <- data)
            yield ((element.asInstanceOf[JsObject] \ "address").as[String],
              BigDecimal((element.asInstanceOf[JsObject] \ "quantity").as[String]))
          (asset, holders)
      }
    })
  }

  private def saveAssetsAndHolders(assetsAndHolders: Seq[(Asset, Seq[(String, BigDecimal)])], collection: Collection): Unit = {
    assetsAndHolders.foreach({
      case (asset, holders) =>
        val doc = Document(
          "name" -> asset.asset,
          "description" -> asset.description,
          "total" -> asset.total.toDouble,
          "divisible" -> asset.divisible,
          "locked" -> asset.locked,
          "addresses" -> holders.map(holder => holder._1),
          "quantities" -> holders.map(holder => holder._2)
        )

        collection.insert(doc)
    })
  }
}
