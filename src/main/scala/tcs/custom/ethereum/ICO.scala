package tcs.custom.ethereum

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import java.security.SecureRandom

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import tcs.custom.ethereum.ICOAPIs.EthplorerAPIs.EthplorerAPI

import scalaj.http.Http
import tcs.custom.ethereum.ICOAPIs.ICOBenchAPIs.{Exchanges, ICOBenchAPI}
import tcs.custom.ethereum.ICOAPIs.etherScanAPIs.EtherScanAPI
import tcs.custom.ethereum.ICOAPIs.tokenWhoIsAPIs.TokenWhoIsAPI

class ICO {

  private var name: String = _
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

  def this(nameOrAddress: String) {
    this
    if(nameOrAddress.startsWith("0x") && nameOrAddress.lengthCompare(10) > 0){
      this.contractAddress = nameOrAddress
    }
    else{
      this.name = nameOrAddress
    }
    EthplorerAPI.checkIfTokenExists(nameOrAddress)
  }

  /**
    * @return ICO's name
    */
  def getName: String = {
    if(this.name == null){
      this.name = EthplorerAPI.getTokenNameByContractAddress(this.getContractAddress)
    }
    this.name
  }

  /**
    * @return ICO's symbol
    */
  def getSymbol: String = {
    if (this.symbol == null) {
      try {
        val ico = ICOBenchAPI.getICOByName(this.getName)
        this.symbol = ico.finance.token
      } catch {
        case e: Exception => {
          this.symbol = EthplorerAPI.getTokenSymbolByContractAddress(this.getContractAddress)
        }
      }
    }
    this.symbol
  }

  /**
    * @return ICO's contract address
    */
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

  /**
    * @return ICO's total supply
    */
  def getTotalSupply: Double = {
    if (this.totalSupply == -1) {
      this.totalSupply = EtherScanAPI.getTotalSupplyByAddress(
        this.getContractAddress
      )
    }
    this.totalSupply
  }

  /**
    * @return ICO's Market Capitalization
    */
  def getMarketCap: Double = {
    if (this.marketCap == -1) {
      this.marketCap = TokenWhoIsAPI.getMarketCap(
        this.getName
      )
    }
    this.marketCap
  }

  /**
    * @return Blockchain used by this ICO
    */
  def getBlockchain: String = {
    if (this.usedBlockchain == null) {
      this.usedBlockchain = TokenWhoIsAPI.getUsedBlockchain(
        this.getName
      )
    }
    this.usedBlockchain
  }

  /**
    * @param address wallet address
    * @return token owned by this address
    */
  def getAddressBalance(address: String): Double = {
    EtherScanAPI.getTokenAccountBalance(
      this.getContractAddress, address
    )
  }

  /**
    * @return token unit price (USD)
    */
  def getUSDPrice: Double = {
    if(this.USDPrice == -1) {
      var price = TokenWhoIsAPI.getUSDUnitPrice(
        this.getName
      )
      if(price == 0) {
        price = EthplorerAPI.getTokenPriceByContractAddress(this.getContractAddress, "USD")
      }
      this.USDPrice = price
    }
    this.USDPrice
  }

  /**
    * @return token unit price (ETH)
    */
  def getETHPrice: Double = {
    if(this.ETHPrice == -1) {
      var price = TokenWhoIsAPI.getETHUnitPrice(
        this.getName, this.getSymbol
      )
      if(price == 0) {
        price = EthplorerAPI.getTokenPriceByContractAddress(this.getContractAddress, "ETH")
      }
      this.ETHPrice = price
    }
    this.ETHPrice
  }

  /**
    * @return token unit price (BTC)
    */
  def getBTCPrice: Double = {
    if(this.BTCPrice == -1) {
      var price = TokenWhoIsAPI.getBTCUnitPrice(
        this.getName
      )
      if(price == 0) {
        price = EthplorerAPI.getTokenPriceByContractAddress(this.getContractAddress, "BTC")
      }
      this.BTCPrice = price
    }
    this.BTCPrice
  }

  /**
    * @return Hype score given by ICORating
    */
  def getHypeScore: Float = {
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

  /**
    * @return Investment Rating given by ICORating
    */
  def getInvestmentRating: String = {
    if (this.investmentRating == null) {
      this.investmentRating = getScore("Investment")
    }
    this.investmentRating
  }

  /**
    * @return Risk score given by ICORating
    */
  def getRiskScore: Float = {
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

  /**
    * @return Name of the exchanges that trade this token
    */
  def getExchangesNames: Array[String] = {
    TokenWhoIsAPI.getExchangesNames(
      this.name
    )
  }

  /**
    * @return Details of the exchanges that trade this token
    */
  def getExchangesDetails: Array[Exchanges] = {
    ICOBenchAPI.getExchanges(this.name)
  }

  private def getScore(scoreType: String): String = {
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
