package tcs.custom.ethereum.ICOAPIs.ICOBenchAPIs

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.google.common.io.BaseEncoding
import tcs.custom.ethereum.Utils

import scala.collection.mutable
import scalaj.http.{Http, HttpRequest}

/**
  * Object that provides methods to ICOBench API
  */
object ICOBenchAPI {
  private val privateKey = "privateKey"
  private val publicKey = "publicKey"
  private val apiUrl = "https://icobench.com/api/v1/"

  private val fixedHeaders: mutable.Map[String, String] = mutable.Map(
    "Content-Type" -> "application/json",
    "X-ICObench-Key" -> this.publicKey
  )

  /**
    * @param data filters for data
    * @return A list of ICOS (ICOBenchResult.results)
    */
  def getAllICOs(data: Map[String, Any] = Map()): ICOBenchResult = {
    val url = String.join("/", this.apiUrl, "icos/all")
    val jsonData = toJSONString(data)
    val httpRequest = send(url, jsonData)

    val result = Utils.getMapper.readValue[ICOBenchResult](httpRequest.asString.body)
    result
  }

  /**
    * @param icoID id of an ico inside ICOBench
    * @return Detailed result of this ICO
    */
  def getICOByICOBenchID(icoID: Int): ICOVerboseResult = {
    val url = String.join("/", this.apiUrl, "ico", icoID.toString)
    val jsonData = toJSONString(Map[String, Any]())
    val httpRequest = send(url, jsonData)

    Utils.getMapper.readValue[ICOVerboseResult](httpRequest.asString.body)
  }

  /**
    * @param name ICOName
    * @return Detailed result of this ICO
    */
  def getICOByName(name: String): ICOVerboseResult = {
    var benchResult: Array[ICOShortResult] = Array()

    Utils.prepareNames(name).iterator.takeWhile(_ => benchResult.nonEmpty).foreach(
      result => {
        benchResult = this.getAllICOs(Map("search" -> name)).results
          .filter(ico => ico.name.toLowerCase.contains(name.toLowerCase))
      }
    )
    this.getICOByICOBenchID(benchResult.head.id)
  }

  def getICOBySymbol(symbol: String): ICOVerboseResult = {
    this.getAllICOs(Map("search" -> symbol)).results
      .map(icoRes => {
        this.getICOByName(icoRes.name)
      })
      .filter(icoVerbRes => {
        icoVerbRes.finance.token.equals(symbol)
      }).head
  }

  /**
    * @param data filters for data
    * @return All ICOs that have received rating for either ICO profile or by experts
    */
  def getAllICORatings(data: Map[String, Any] = Map()): ICOBenchResult = {
    val url = String.join("/", this.apiUrl, "icos/ratings")
    val jsonData = toJSONString(data)
    val httpRequest = send(url, jsonData)

    Utils.getMapper.readValue[ICOBenchResult](httpRequest.asString.body)
  }

  /**
    * @return up to 8 ICOs that are currently "Hot and Trending" on ICObench
    */
  def getTrending: Array[ICOShortResult] = {
    val url = String.join("/", this.apiUrl, "icos", "trending")
    val httpRequest = send(url, "{}")

    Utils.getMapper.readValue[ICOBenchResult](httpRequest.asString.body).results
  }

  /**
    * @return all available filters in ICOBench
    */
  def getFilters: ICOFiltersResult = {
    val url = String.join("/", this.apiUrl, "icos", "filters")
    val httpRequest = send(url, "{}")

    Utils.getMapper.readValue[ICOFiltersResult](httpRequest.asString.body)
  }

  /**
    * @return short and interesting statistics about ICObench
    */
  def getStats: ICOStatsResult = {
    val url = String.join("/", this.apiUrl, "other", "stats")
    val httpRequest = send(url, "{}")

    Utils.getMapper.readValue[ICOStatsResult](httpRequest.asString.body)
  }

  /**
    * @param name ICOName
    * @return information about Exchanges that trade this token
    */
  def getExchanges(name: String): Array[Exchanges] = {
    this.getICOByName(name).exchanges
  }

  private def send(url: String, data: String): HttpRequest = {
    val secret = new SecretKeySpec(this.privateKey.getBytes, "SHA384")
    val mac = Mac.getInstance("HmacSHA384")
    mac.init(secret)
    val sig = BaseEncoding.base64().encode(mac.doFinal(data.getBytes))

    var currentHeaders = fixedHeaders.clone
    currentHeaders += (
      "Content-Length" -> data.length.toString,
      "X-ICObench-Sig" -> sig
    )

    Http(url).headers(currentHeaders.toMap).postData(data)
  }

  private def toJSONString(data: Map[String, Any]): String = {
    Utils.getMapper.writeValueAsString(data)
  }
}
