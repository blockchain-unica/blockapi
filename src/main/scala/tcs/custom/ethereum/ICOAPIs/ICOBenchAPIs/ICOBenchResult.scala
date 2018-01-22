package tcs.custom.ethereum.ICOAPIs.ICOBenchAPIs

/**
  * Pojo classes for icos/all API
  */
case class ICOBenchResult(
                      icos: Int,
                      pages: Int,
                      currentPage: Int,
                      results: Array[ICOShortResult]
                      )

case class ICOShortResult(
                           id: Int,
                           name: String,
                           url: String,
                           logo: String,
                           desc: String,
                           rating: Float,
                           premium: Int,
                           raised: Int,
                           dates: ICODates
                         )


case class ICODates(
                     preIcoStart: String,
                     preIcoEnd: String,
                     icoStart: String,
                     icoEnd: String
                   )
