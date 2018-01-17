package tcs.custom.ethereum.ICOBenchAPIs

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.google.common.io.BaseEncoding

import scala.collection.mutable
import scalaj.http.{Http, HttpRequest}


object ICOBenchAPI {
  private val privateKey = "private-key"
  private val publicKey = "public-key"
  private val apiUrl = "https://icobench.com/api/v1/"

  private val fixedHeaders: mutable.Map[String, String] = mutable.Map(
    "Content-Type" -> "application/json",
    "X-ICObench-Key" -> this.publicKey
  )


  def getAllICOs(data: Map[String, Any] = Map()): ICOBenchResult = {
    val url = String.join("/", this.apiUrl, "icos/all")
    val jsonData = toJSONString(data)
    val httpRequest = send(url, jsonData)

    val mapper = getMapper
    val result = mapper.readValue[ICOBenchResult](httpRequest.asString.body)
    result
  }

  def getICOByICOBenchID(icoID: Int, data: Map[String, Any] = Map()): ICOVerboseResult = {
    val url = String.join("/", this.apiUrl, "ico", icoID.toString)
    val jsonData = toJSONString(data)
    val httpRequest = send(url, jsonData)

    val mapper = getMapper
    val result = mapper.readValue[ICOVerboseResult](httpRequest.asString.body)
    result
  }

  def getICOByName(name: String): ICOVerboseResult = {
    val benchResult = this.getAllICOs(Map("search" -> name))
      .results.filter(ico => ico.name.equals(name)).head
    this.getICOByICOBenchID(benchResult.id)
  }

  def getAllICORatings(data: Map[String, Any] = Map()): ICOBenchResult = {
    val url = String.join("/", this.apiUrl, "icos/ratings")
    val jsonData = toJSONString(data)
    val httpRequest = send(url, jsonData)

    val mapper = getMapper
    val result = mapper.readValue[ICOBenchResult](httpRequest.asString.body)
    result
  }

  def getTrending: Array[ICOShortResult] = {
    val url = String.join("/", this.apiUrl, "icos", "trending")
    val httpRequest = send(url, "{}")

    val mapper = getMapper
    val result = mapper.readValue[ICOBenchResult](httpRequest.asString.body)
    result.results
  }

  def getFilters: ICOFiltersResult = {
    val url = String.join("/", this.apiUrl, "icos", "filters")
    val httpRequest = send(url, "{}")

    val mapper = getMapper
    val result = mapper.readValue[ICOFiltersResult](httpRequest.asString.body)
    result
  }

  def getStats: ICOStatsResult = {
    val url = String.join("/", this.apiUrl, "other", "stats")
    val httpRequest = send(url, "{}")

    val mapper = getMapper
    val result = mapper.readValue[ICOStatsResult](httpRequest.asString.body)
    result
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

  def getMapper: ObjectMapper with ScalaObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
      .asInstanceOf[ObjectMapper with ScalaObjectMapper]
  }

  private def toJSONString(data: Map[String, Any]): String = {
    val mapper = getMapper
    mapper.writeValueAsString(data)
  }
}
