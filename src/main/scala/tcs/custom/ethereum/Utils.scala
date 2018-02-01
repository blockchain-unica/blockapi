package tcs.custom.ethereum

import java.security.cert.X509Certificate
import javax.net.ssl.{TrustManager, X509TrustManager}

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object Utils {

  /**
    * Used to fix certificate problems
    */
  val trustAllCerts: Array[TrustManager] = Array[TrustManager](
    new X509TrustManager() {
      def getAcceptedIssuers: Array[X509Certificate] = null

      def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}

      def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
    }
  )

  /**
    * @return Mapper used to create pojo classes
    */
  def getMapper: ObjectMapper with ScalaObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
      .asInstanceOf[ObjectMapper with ScalaObjectMapper]
  }

  /**
    * @param tokenName
    * @return possible names for apis
    */
  def prepareNames(tokenName: String): Array[String] = {
    var preparedNames = Array[String]()
    preparedNames = preparedNames :+ tokenName
    preparedNames = preparedNames :+ tokenName.replace(" ", "-")
    preparedNames = preparedNames :+ tokenName.toLowerCase
    preparedNames = preparedNames :+ tokenName.toLowerCase.replace(" ", "-")
    val names = tokenName.split(" ")
    preparedNames = preparedNames ++ names
    for (i <- 2 to names.length) {
      preparedNames = preparedNames ++ names.combinations(i).map(
        a => a.mkString("")
      ).toList
      preparedNames = preparedNames ++ names.combinations(i).map(
        a => a.mkString("-")
      ).toList
    }
    preparedNames.distinct
  }
}
