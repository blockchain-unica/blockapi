package tcs.custom.ethereum.ICOAPIs.coinMarketCapAPIs

import java.net.SocketTimeoutException

import tcs.custom.ethereum.Utils

import scalaj.http.Http

object CoinMarketCapAPI {

  def getTokenMarketCap(tokenName: String, tokenSymbol: String): Double = {
    val elementList = send().filter(element => {
      element.id == tokenName || element.name == tokenName || element.symbol == tokenSymbol
    })
    if(elementList.nonEmpty){
      if(elementList.head.market_cap_usd != null){
        elementList.head.market_cap_usd.toDouble
      } else {
        0
      }
    } else {
      0
    }
  }

  def getTokenPriceUSD(tokenName: String, tokenSymbol: String): Double = {
    val elementList = send().filter(element => {
      element.id == tokenName || element.name == tokenName || element.symbol == tokenSymbol
    })
    if(elementList.nonEmpty){
      if(elementList.head.market_cap_usd != null){
        elementList.head.price_usd.toDouble
      } else {
        0
      }
    } else {
      0
    }
  }

  def getTokenPriceBTC(tokenName: String, tokenSymbol: String): Double = {
    val elementList = send().filter(element => {
      element.id == tokenName || element.name == tokenName || element.symbol == tokenSymbol
    })
    if(elementList.nonEmpty){
      if(elementList.head.market_cap_usd != null){
        elementList.head.price_btc.toDouble
      } else {
        0
      }
    } else {
      0
    }
  }

  private def send(): Array[CoinMarketCapResult] = {
    try {
      Utils.getMapper.readValue[Array[CoinMarketCapResult]](
        Http("https://api.coinmarketcap.com/v1/ticker/?limit=0").asString.body
      )
    } catch {
      case _: SocketTimeoutException => {
        send()
      }
    }
  }

}
