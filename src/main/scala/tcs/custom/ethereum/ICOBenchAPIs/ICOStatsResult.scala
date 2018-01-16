package tcs.custom.ethereum.ICOBenchAPIs

case class ICOStatsResult(
                           countriesByIcos: Array[CountryStat],
                           countriesByIcosPerMillion: Array[CountryStat],
                           topRaised: Array[Raised],
                           icos: Int,
                           members: Int,
                           topIss: Array[UserICOSuccessScore]
                         )

case class CountryStat(
                        contry: String,
                        count: Int
                      )

case class Raised(
                   id: Int,
                   name: String,
                   url: String,
                   raised: Long
                 )

case class UserICOSuccessScore(
                                iss: Float,
                                name: String
                              )
