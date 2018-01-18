package tcs.custom.ethereum.tokenWhoIsAPIs

case class TokenWhoIsResponse(
                               events: String,
                               blockchain: String,
                               symbol: String,
                               name: String,
                               totalScore: Int,
                               _category: TokenWhoIsCategory,
                               _geo: ICOGeo,
                               _tags: Array[String],
                               _tech: Array[String],
                               market: Map[String, ICOMarket],
                               btcPrice: Double,
                               change: Double,
                               marketcap: Long,
                               usdPrice: Double,
                               volume: Double,
                               exchanges: Array[String]
                             )

case class TokenWhoIsCategory(
                               naicsCode: String,
                               sicCode: String,
                               subIndustry: String,
                               industry: String,
                               industryGroup: String,
                               sector: String
                             )

case class ICOGeo(
                   lng: Float,
                   lat: Float,
                   countryCode: String,
                   country: String,
                   stateCode: String,
                   state: String,
                   postalCode: String,
                   city: String,
                   subPremise: String,
                   streetName: String,
                   streetNumber: Int
                 )

case class ICOMarkets(
                       ETH: ICOMarket,
                       BTC: ICOMarket,
                       USD: ICOMarket
                     )

case class ICOMarket(
                      TOTALVOLUME24HTO: Double,
                      TOTALVOLUME24H: Double,
                      MKTCAP: Double,
                      SUPPLY: Double,
                      CHANGEPCTDAY: Double,
                      CHANGEDAY: Double,
                      CHANGEPCT24HOUR: Double,
                      CHANGE24HOUR: Double,
                      LASTMARKET: String,
                      LOW24HOUR: Double,
                      HIGH24HOUR: Double,
                      OPEN24HOUR: Double,
                      LOWDAY: Double,
                      HIGHDAY: Double,
                      OPENDAY: Double,
                      VOLUME24HOURTO: Double,
                      VOLUME24HOUR: Double,
                      VOLUMEDAYTO: Double,
                      VOLUMEDAY: Double,
                      LASTTRADEID: String,
                      LASTVOLUMETO: String,
                      LASTVOLUME: Double,
                      LASTUPDATE: Double,
                      PRICE: Double,
                      FLAGS: String,
                      TOSYMBOL: String,
                      FROMSYMBOL: String,
                      MARKET: String,
                      TYPE: String
                    )
