package tcs.custom.ethereum.ICOAPIs.coinMarketCapAPIs

case class CoinMarketCapResult(
                                id: String,
                                name: String,
                                symbol: String,
                                rank: String,
                                price_usd: String,
                                price_btc: String,
                                market_cap_usd: String,
                                available_supply: String,
                                total_supply: String,
                                max_supply: String,
                                percent_change_1h: String,
                                percent_change_24h: String,
                                percent_change_7d: String,
                                last_updated: String
                              )