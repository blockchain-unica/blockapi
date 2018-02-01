package tcs.custom.ethereum.ICOAPIs.tokenWhoIsAPIs

import java.security.SecureRandom
import javax.net.ssl.{HttpsURLConnection, SSLContext}

import scalaj.http.Http
import tcs.custom.ethereum.Utils

object TokenWhoIsAPI {

  /**
    * @param tokenName
    * @return Blockchain used by this token
    */
  def getUsedBlockchain(tokenName: String): String = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).blockchain
    } catch {
      case _: Exception => "Blockchain not Found"
    }
  }

  /**
    * @param tokenName
    * @return Market Cap of this token
    */
  def getMarketCap(tokenName: String): Double = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).marketcap
    } catch {
      case _: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @return Token unit price (USD)
    */
  def getUSDUnitPrice(tokenName: String): Double = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).usdPrice
    } catch {
      case _: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @param tokenSymbol
    * @return Token unit price (ETH)
    */
  def getETHUnitPrice(tokenName: String, tokenSymbol: String): Double = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).market(tokenSymbol).ETH.PRICE
    } catch {
      case _: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @return Token unit price (BTC)
    */
  def getBTCUnitPrice(tokenName: String): Double = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).btcPrice
    } catch {
      case _: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @return name of Exchanges that trade this token
    */
  def getExchangesNames(tokenName: String): Array[String] = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).exchanges
    } catch {
      case _: Exception => Array()
    }
  }

  def getUSDSupply(tokenName: String, tokenSymbol: String): Double = {
    try {
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).market(tokenSymbol).USD.SUPPLY
    } catch {
      case _: Exception => 0
    }
  }

  private def sendRequest(tokenName: String): String = {
    var found: Boolean = false
    var response: String = ""
    val sc = SSLContext.getInstance("SSL")
    sc.init(null, Utils.trustAllCerts, new SecureRandom)
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
    Utils.prepareNames(tokenName).iterator.takeWhile(_ => !found)
      .foreach(
        name => {
          try {
            response = Http(
              String.join(
                "/", "http://tokenwhois.com/api/projects", name
              )
            ).asString.body
            Utils.getMapper.readValue[TokenWhoIsResponse](response)
            found = true
          } catch {
            case _: Exception =>
          }
        }
      )
    response
  }
}
