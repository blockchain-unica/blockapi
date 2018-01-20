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
    try{
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).blockchain
    } catch {
      case e: Exception => "Blockchain not Found"
    }
  }

  /**
    * @param tokenName
    * @return Market Cap of this token
    */
  def getMarketCap(tokenName: String): Double = {
    try{
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).marketcap
    } catch {
      case e: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @return Token unit price (USD)
    */
  def getUSDUnitPrice(tokenName: String): Double = {
    try{
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).usdPrice
    } catch {
      case e: Exception => 0
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
      case e: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @return Token unit price (BTC)
    */
  def getBTCUnitPrice(tokenName: String): Double = {
    try{
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).btcPrice
    } catch {
      case e: Exception => 0
    }
  }

  /**
    * @param tokenName
    * @return name of Exchanges that trade this token
    */
  def getExchangesNames(tokenName: String): Array[String] = {
    try{
      Utils.getMapper.readValue[TokenWhoIsResponse](
        this.sendRequest(tokenName)
      ).exchanges
    } catch {
      case e: Exception => Array()
    }
  }

  private def sendRequest(tokenName: String): String = {
    try {
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, Utils.trustAllCerts, new SecureRandom)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
      Http(
        String.join(
          "/", "http://tokenwhois.com/api/projects", tokenName
        )
      ).asString.body
    } catch {
      case e: Exception =>
        System.out.println(e)
        ""
    }
  }
}
