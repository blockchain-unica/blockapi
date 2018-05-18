package tcs.custom.ethereum.ICOAPIs.etherScanAPIs

import tcs.custom.ethereum.Utils

import scalaj.http.Http

/**
  * Object that provides methods to EtherScan Token API
  */
object EtherScanAPI {

  private def apiKey: String = "apiKey"
  private def weiIntoEth: Long = "1000000000000000000".toLong

  /**
    * @param address the ERC20-Token contract Address
    * @return total supply of token
    */
  def getTotalSupplyByAddress(address: String): Double = {
    Option[Double](
      Utils.getMapper.readValue[EtherScanTokenAmountResponse](
        Http(
          String.join(
            "", "https://api.etherscan.io/api?module=stats&action=tokensupply&contractaddress=", address, "&apikey=", apiKey
          )
        ).asString.body
      ).result.toDouble
    ).getOrElse(0: Double)
  }

  /**
    * @param tokenContractAddress the ERC20-Token contract Address
    * @param userAddress wallet address
    * @return tokens contained in this wallet
    */
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
    ).getOrElse(0: Double)
  }
}
