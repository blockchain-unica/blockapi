package tcs.custom.ethereum.ICOAPIs.EthplorerAPIs

import tcs.custom.ethereum.Utils

import scalaj.http.Http

object EthplorerAPI {

  private def apiKey = "freekey"

  private def baseUrl = "https://api.ethplorer.io/"

  /**
    * @param tokenAddress
    * @return token Name
    */
  def getTokenNameByContractAddress(tokenAddress: String): String = {
    Utils.getMapper.readValue[EthplorerTokenInfo](
      send(
        String.join("", this.baseUrl, "getTokenInfo/", tokenAddress, "?apiKey=", apiKey)
      )
    ).name
  }

  /**
    * @param tokenAddress
    * @return token symbol
    */
  def getTokenSymbolByContractAddress(tokenAddress: String): String = {
    Utils.getMapper.readValue[EthplorerTokenInfo](
      send(
        String.join("", this.baseUrl, "getTokenInfo/", tokenAddress, "?apiKey=", apiKey)
      )
    ).symbol
  }

  /**
    * @param tokenAddress
    * @param currency
    * @return token unit price
    */
  def getTokenPriceByContractAddress(tokenAddress: String, currency: String): Double = {
    val price = Utils.getMapper.readValue[EthplorerTokenInfo](
      send(
        String.join("", this.baseUrl, "getTokenInfo/", tokenAddress, "?apiKey=", apiKey)
      )
    ).price
    try{
      if(price("currency").equals(currency)){
        price("rate").asInstanceOf[String].toDouble
      } else {
        0
      }
    } catch {
      case e: Exception => 0
    }
  }

  def checkIfTokenExists(tokenAddress: String): Any = {
    Utils.getMapper.readValue[EthplorerTokenInfo](
      send(
        String.join("", this.baseUrl, "getTokenInfo/", tokenAddress, "?apiKey=", apiKey)
      )
    )
  }

  private def send(url: String): String = {
    Http(url).asString.body
  }
}
