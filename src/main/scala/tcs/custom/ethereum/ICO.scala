package tcs.custom.ethereum

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import java.security.SecureRandom

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scalaj.http.Http

import ICOBenchAPIs.ICOBenchAPI
import tcs.custom.ethereum.etherScanAPIs.EtherScanAPI
import tcs.custom.ethereum.tokenWhoIsAPIs.TokenWhoIsAPI

class ICO(private val name: String) {

  private var symbol: String = _
  private var contractAddress: String = _
  private var totalSupply: Double = -1
  private var marketCap: Double = -1
  private var usedBlockchain: String = _
  private var USDPrice: Double = -1
  private var ETHPrice: Double = -1
  private var BTCPrice: Double = -1
  private var hypeScore: Float = -1
  private var riskScore: Float = -1
  private var investmentRating: String = _

  private var browser: JsoupDocument = _

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
        sc.init(null, Utils.trustAllCerts, new SecureRandom)
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

  def getMarketCap: Double = {
    if (this.marketCap == -1) {
      this.marketCap = TokenWhoIsAPI.getMarketCap(
        this.name
      )
    }
    this.marketCap
  }

  def getBlockchain: String = {
    if (this.usedBlockchain == null) {
      this.usedBlockchain = TokenWhoIsAPI.getUsedBlockchain(
        this.name
      )
    }
    this.usedBlockchain
  }

  def getAddressBalance(address: String): Double = {
    EtherScanAPI.getTokenAccountBalance(
      this.getContractAddress, address
    )
  }

  def getUSDPrice: Double = {
    if(this.USDPrice == -1) {
      this.USDPrice = TokenWhoIsAPI.getUSDUnitPrice(
        this.getName
      )
    }
    this.USDPrice
  }

  def getETHPrice: Double = {
    if(this.ETHPrice == -1) {
      this.ETHPrice = TokenWhoIsAPI.getETHUnitPrice(
        this.getName, this.getSymbol.toUpperCase
      )
    }
    this.ETHPrice
  }

  def getBTCPrice: Double = {
    if(this.BTCPrice == -1) {
      this.BTCPrice = TokenWhoIsAPI.getBTCUnitPrice(
        this.getName
      )
    }
    this.BTCPrice
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
      sc.init(null, Utils.trustAllCerts, new SecureRandom)
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
