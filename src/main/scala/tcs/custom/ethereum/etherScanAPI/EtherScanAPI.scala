package tcs.custom.ethereum.etherScanAPI

import tcs.custom.ethereum.Utils

import scalaj.http.Http

object EtherScanAPI {

  private def apiKey: String = "apiKey"
  private def weiIntoEth: Long = "1000000000000000000".toLong

  def getTotalSupplyByAddress(address: String): Double = {
    Option[Double](
      Utils.getMapper.readValue[EtherScanTokenAmountResponse](
        Http(
          String.join(
            "", "https://api.etherscan.io/api?module=stats&action=tokensupply&contractaddress=", address, "&apikey=", apiKey
          )
        ).asString.body
      ).result.toDouble
    ).getOrElse(0: Double) / this.weiIntoEth
  }

  def getTokenAccountBalance(tokenContractAddress: String, userAddress: String): Double = {
    Option[Double](
      Utils.getMapper.readValue[EtherScanTokenAmountResponse](
        Http(
          String.join(
            "", "https://api.etherscan.io/api?module=account&action=tokenbalance&contractaddress=",
            tokenContractAddress, "&address=", userAddress, "&apikey=", apiKey
          )
        ).asString.body
      ).result.toDouble
    ).getOrElse(0: Double) / this.weiIntoEth
  }
}
