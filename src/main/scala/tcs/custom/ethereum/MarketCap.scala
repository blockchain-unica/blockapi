package tcs.custom.ethereum

case class MarketCap(
                      date: String,
                      timestamp: BigDecimal,
                      supply: BigDecimal,
                      marketCap: BigDecimal
                    )