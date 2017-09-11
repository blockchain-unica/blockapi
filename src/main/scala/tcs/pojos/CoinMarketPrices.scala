package tcs.pojos

/**
  * Created by ferruvich on 16/08/2017.
  */
case class CoinMarketPrices(
                        market_cap_by_available_supply: Map[String, Int],
                        price_btc: Map[String, Double],
                        price_usd: Map[String, Double]
                      )

