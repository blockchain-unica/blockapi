package tcs.custom.ethereum

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import java.security.SecureRandom

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._


class ICO(
         name: String
         ) {
  private var browser: JsoupDocument = _

  val trustAllCerts: Array[TrustManager] = Array[TrustManager](
    new X509TrustManager() {
      def getAcceptedIssuers: Array[X509Certificate] = null

      def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
      def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
  })

  private def initializeBrowser(): Unit = {
    if(browser == null){
      browser = new JsoupBrowser().get("https://icorating.com/ico/" + this.name.replace(" ", "-").toLowerCase)
    }
  }

  private def getScore(scoreType: String): Any = {
    try {
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new SecureRandom)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
      initializeBrowser()
      val scorePart = browser >> elementList("div .white-block-area div div")
      val hypeDoc = scorePart.filter(element => {
        (element >> allText(".title")).contains(scoreType)
      }).head
      var hypeScore = hypeDoc >> allText(".score")
      if (hypeScore.isEmpty) {
        hypeScore = hypeDoc >> allText(".name")
      }
      hypeScore
    } catch {
      case e: Exception =>
        System.out.println(e)
        ""
    }
  }

  def getHypeScore(): Any = {
    getScore("Hype")
  }

  def getInvestmentRating(): Any = {
    getScore("Investment")
  }

  def getRiskScore(): Any = {
    getScore("Risk")
  }
}
