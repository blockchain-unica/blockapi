package tcs.custom.ethereum

import java.security.cert.X509Certificate
import javax.net.ssl.{TrustManager, X509TrustManager}

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object Utils {
  /**
    * @return Mapper used to create pojo classes
    */
  def getMapper: ObjectMapper with ScalaObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
      .asInstanceOf[ObjectMapper with ScalaObjectMapper]
  }

  /**
    * Used to fix certificate problems
    */
  val trustAllCerts: Array[TrustManager] = Array[TrustManager](
    new X509TrustManager() {
      def getAcceptedIssuers: Array[X509Certificate] = null

      def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}

      def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
    })
}
