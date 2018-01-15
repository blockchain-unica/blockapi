package tcs.custom.ethereum.ICOBenchAPIs

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.google.common.io.BaseEncoding

import scala.collection.mutable
import scalaj.http.Http


object ICOBenchAPI {
  private val privateKey	= "f054358a-5e1b-4503-90db-ba0f6a457c60"
  private val publicKey	= "23773421-a69e-446f-8207-25f375467020"
  private val apiUrl		= "https://icobench.com/api/v1/"

  private val fixedHeaders: mutable.Map[String, String] = mutable.Map(
    "Content-Type" -> "application/json",
    "X-ICObench-Key" -> this.publicKey
  )


  def getAllICOs(icoID: String = "all", data: Map[String, Any] = Map()): BenchResult = {
    val url = String.join("/", this.apiUrl, "icos", icoID)

    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val jsonData = mapper.writeValueAsString(data)

    send(url, jsonData)
  }


  private def send(url: String, data: String): BenchResult = {
    val secret = new SecretKeySpec(this.privateKey.getBytes, "SHA384")
    val mac = Mac.getInstance("HmacSHA384")
    mac.init(secret)
    val sig = BaseEncoding.base64().encode(mac.doFinal(data.getBytes))

    var currentHeaders = fixedHeaders.clone
    currentHeaders += (
      "Content-Length" -> data.length.toString,
      "X-ICObench-Sig" -> sig
    )

    val result = Http(url).headers(currentHeaders.toMap).postData(data)

    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val benchResult = mapper.readValue[BenchResult](result.asString.body)
    benchResult
  }
}
