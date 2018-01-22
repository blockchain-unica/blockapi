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

  private def send(url: String): String = {
    Http(url).asString.body
  }
}
