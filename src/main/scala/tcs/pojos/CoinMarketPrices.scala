package tcs.pojos

import java.util.Date


/**
  * Created by ferruvich on 16/08/2017.
  */
case class CoinMarketPrices(
                        market_cap_by_available_supply: Map[Date, Int],
                        price_btc: Map[Date, Double],
                        price_usd: Map[Date, Double]
                      )

