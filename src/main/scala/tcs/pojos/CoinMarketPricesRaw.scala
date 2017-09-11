package tcs.pojos

/**
  * Created by ferruvich on 16/08/2017.
  */
case class CoinMarketPricesRaw(
                        market_cap_by_available_supply: List[List[BigDecimal]],
                        price_btc: List[List[BigDecimal]],
                        price_usd: List[List[BigDecimal]]
                      )
