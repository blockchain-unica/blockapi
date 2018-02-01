package tcs.custom.ethereum

import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

import scalaj.http.Http
import tcs.custom.ethereum.ICOAPIs.ethplorerAPIs.EthplorerAPI
import tcs.custom.ethereum.ICOAPIs.ICOBenchAPIs.{Exchanges, ICOBenchAPI}
import tcs.custom.ethereum.ICOAPIs.ICORatingAPIs.ICORatingAPI
import tcs.custom.ethereum.ICOAPIs.coinMarketCapAPIs.CoinMarketCapAPI
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
  private var exists: Boolean = false

  def this(nameOrAddress: String) {
    this
    if (nameOrAddress.startsWith("0x") && nameOrAddress.lengthCompare(10) > 0) {
      this.contractAddress = nameOrAddress
    }
    else {
      this.name = nameOrAddress
    }

    val pseudoToken = EthplorerAPI.checkIfTokenExists(nameOrAddress)
    if (pseudoToken.name == null && pseudoToken.address == null) {
      this.exists = false
      println(nameOrAddress + " is not a token contract")
    } else {
      this.exists = true
    }
  }

  /**
    * @return ICO's name
    */
  def getName: String = {
    if (this.name == null) {
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
      if (this.totalSupply == 0) {
        this.totalSupply = TokenWhoIsAPI.getUSDSupply(
          this.getName, this.getSymbol
        )
      }
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
      if (this.marketCap == 0) {
        this.marketCap = CoinMarketCapAPI.getTokenMarketCap(
          this.getName, this.getSymbol
        )
      }
      if (this.marketCap == 0) {
        try{
          this.marketCap = ICOBenchAPI.getICOByName(
            this.getName
          ).finance.raised
        } catch {
          case _ : Exception => this.marketCap = 0
        }
      }
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
    if (this.USDPrice.compareTo(-1) == 0) {
      var price = TokenWhoIsAPI.getUSDUnitPrice(
        this.getName
      )
      if (price.compareTo(0) == 0) {
        price = EthplorerAPI.getTokenPriceByContractAddress(this.getContractAddress, "USD")
      }
      if (price.compareTo(0) == 0) {
        price = CoinMarketCapAPI.getTokenPriceUSD(
          this.getName, this.getSymbol
        )
      }
      this.USDPrice = price
    }
    this.USDPrice
  }

  /**
    * @return token unit price (ETH)
    */
  def getETHPrice: Double = {
    if (this.ETHPrice == -1) {
      var price = TokenWhoIsAPI.getETHUnitPrice(
        this.getName, this.getSymbol
      )
      if (price == 0) {
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
    if (this.BTCPrice == -1) {
      var price = TokenWhoIsAPI.getBTCUnitPrice(
        this.getName
      )
      if (price == 0) {
        price = EthplorerAPI.getTokenPriceByContractAddress(this.getContractAddress, "BTC")
      }
      if (price == 0) {
        price = CoinMarketCapAPI.getTokenPriceBTC(
          this.getName, this.getSymbol
        )
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
      this.hypeScore = ICORatingAPI.getHypeScore(this.getName)
    }
    this.hypeScore
  }

  /**
    * @return Investment Rating given by ICORating
    */
  def getInvestmentRating: String = {
    if (this.investmentRating == null) {
      this.investmentRating = ICORatingAPI.getInvestmentRating(this.getName)
    }
    this.investmentRating
  }

  /**
    * @return Risk score given by ICORating
    */
  def getRiskScore: Float = {
    if (this.riskScore == -1) {
      this.riskScore = ICORatingAPI.getRiskScore(this.getName)
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

  /**
    * @return
    */
  def itExists: Boolean = {
    this.exists
  }
}
