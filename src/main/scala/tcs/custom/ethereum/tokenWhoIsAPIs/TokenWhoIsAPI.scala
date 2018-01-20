package tcs.custom.ethereum.tokenWhoIsAPIs

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
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).blockchain
  }

  /**
    * @param tokenName
    * @return Market Cap of this token
    */
  def getMarketCap(tokenName: String): Double = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).marketcap
  }

  /**
    * @param tokenName
    * @return Token unit price (USD)
    */
  def getUSDUnitPrice(tokenName: String): Double = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).usdPrice
  }

  /**
    * @param tokenName
    * @param tokenSymbol
    * @return Token unit price (ETH)
    */
  def getETHUnitPrice(tokenName: String, tokenSymbol: String): Double = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).market(tokenSymbol).ETH.PRICE
  }

  /**
    * @param tokenName
    * @return Token unit price (BTC)
    */
  def getBTCUnitPrice(tokenName: String): Double = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).btcPrice
  }

  /**
    * @param tokenName
    * @return name of Exchanges that trade this token
    */
  def getExchangesNames(tokenName: String): Array[String] = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).exchanges
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
