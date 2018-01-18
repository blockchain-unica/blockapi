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

import scalaj.http.Http
import ICOBenchAPIs.ICOBenchAPI
import tcs.custom.ethereum.etherScanAPI.EtherScanAPI

class ICO(private val name: String) {

  private var symbol: String = _
  private var contractAddress: String = _
  private var totalSupply: Double = -1
  private var hypeScore: Float = -1
  private var riskScore: Float = -1
  private var investmentRating: String = _

  private var browser: JsoupDocument = _

  private val trustAllCerts: Array[TrustManager] = Array[TrustManager](
    new X509TrustManager() {
      def getAcceptedIssuers: Array[X509Certificate] = null

      def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}

      def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
    })

  def getName: String = {
    this.name
  }

  def getSymbol: String = {
    if (this.symbol == null) {
      this.symbol = ICOBenchAPI.getICOByName(this.getName).finance.token
    }
    this.symbol
  }

  def getContractAddress: String = {
    if (this.contractAddress == null) {
      try {
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, new SecureRandom)
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
        this.contractAddress =
          Utils.getMapper.readValue[Any](
            Http("https://raw.githubusercontent.com/kvhnuke/etherwallet/mercury/app/scripts/tokens/ethTokens.json")
              .asString.body
          ).asInstanceOf[List[Any]]
            .filter(elem => {
              elem.asInstanceOf[Map[String, Any]].get("symbol").contains(this.getSymbol)
            }).head.asInstanceOf[Map[String, Any]]("address").toString
      } catch {
        case e: Exception =>
          System.out.println(e)
      }
    }
    this.contractAddress
  }

  def getTotalSupply: Double = {
    if (this.totalSupply == -1) {
      this.totalSupply = EtherScanAPI.getTotalSupplyByAddress(
        this.getContractAddress
      )
    }
    this.totalSupply
  }

  def getAddressBalance(address: String): Double = {
    EtherScanAPI.getTokenAccountBalance(
      this.getContractAddress, address
    )
  }

  def getHypeScore: Any = {
    if (this.hypeScore == -1) {
      val score = getScore("Hype")
      var scoreString = score.asInstanceOf[String]
      scoreString = scoreString.substring(0, scoreString.indexOf("/"))
      val scoreFloat = Option[Float](scoreString.toFloat)
      if (scoreFloat.isEmpty) {
        score
      }
      this.hypeScore = scoreFloat.get
    }
    this.hypeScore
  }

  def getInvestmentRating: Any = {
    if (this.investmentRating == null) {
      this.investmentRating = getScore("Investment").asInstanceOf[String]
    }
    this.investmentRating
  }

  def getRiskScore: Any = {
    if (this.riskScore == -1) {
      val score = getScore("Risk")
      var scoreString = score.asInstanceOf[String]
      scoreString = scoreString.substring(0, scoreString.indexOf("/"))
      val scoreFloat = Option[Float](scoreString.toFloat)
      if (scoreFloat.isEmpty) {
        score
      }
      this.riskScore = scoreFloat.get
    }
    this.riskScore
  }

  private def getScore(scoreType: String): Any = {
    try {
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new SecureRandom)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
      initializeBrowser(String.join("/", "https://icorating.com/ico", this.name.replace(" ", "-").toLowerCase))
      val scoreParts = this.browser >> elementList("div .white-block-area div div")
      val scoreDoc = scoreParts.filter(element => {
        (element >> allText(".title")).contains(scoreType)
      }).head
      var score = scoreDoc >> allText(".score")
      if (score.isEmpty) {
        score = scoreDoc >> allText(".name")
      }
      score
    } catch {
      case e: Exception =>
        System.out.println(e)
        ""
    }
  }

  private def initializeBrowser(page: String): Unit = {
    if (!(this.browser == null)) {
      if (!this.browser.underlying.baseUri.equals(page)) {
        this.browser = new JsoupBrowser().get(page)
      }
    } else {
      this.browser = new JsoupBrowser().get(page)
    }
  }
}
