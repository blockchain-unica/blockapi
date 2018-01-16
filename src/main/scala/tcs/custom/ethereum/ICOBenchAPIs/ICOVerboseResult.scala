package tcs.custom.ethereum.ICOBenchAPIs

case class ICOVerboseResult(
                             id: Int,
                             name: String,
                             rating: Float,
                             ratingTeam: Float,
                             ratingVision: Float,
                             ratingProduct: Float,
                             ratingProfile: Float,
                             url: String,
                             tagline: String,
                             intro: String,
                             about: String,
                             logo: String,
                             country: String,
                             milestones: Array[Milestone],
                             teamIntro: String,
                             links: Links,
                             finance: Finance,
                             dates: ICODates,
                             team: Array[TeamMate],
                             ratings: Array[Rating],
                             categories: Array[Category],
                             exchanges: Array[Exchanges]
                           )

case class Milestone(
                      title: String,
                      content: String
                    )

case class Links(
                  twitter: String,
                  slack: String,
                  telegram: String,
                  facebook: String,
                  medium: String,
                  bitcointalk: String,
                  github: String,
                  reddit: String,
                  discord: String,
                  youtube: String,
                  www: String,
                  whitepaper: String
                )

case class Finance(
                    token: String,
                    price: String,
                    bonus: Boolean,
                    tokens: Long,
                    tokentype: String,
                    hardcap: String,
                    softcap: String,
                    raised: Long,
                    platform: String,
                    distributed: String,
                    minimum: String,
                    accepting: String
                  )

case class TeamMate(
                     name: String,
                     title: String,
                     links: String,
                     group: String,
                     photo: String,
                     iss: Float
                   )

case class Rating(
                   date: String,
                   name: String,
                   title: String,
                   photo: String,
                   team: Int,
                   vision: Int,
                   product: Int,
                   profile: Int,
                   review: String,
                   weight: String,
                   agree: Int
                 )

case class Category(
                     id: Int,
                     name: String
                   )

case class Exchanges(
                      id: Int,
                      name: String,
                      logo: String,
                      price: Float,
                      currency: String,
                      roi: String
                    )
