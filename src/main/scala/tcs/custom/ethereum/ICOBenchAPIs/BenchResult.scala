package tcs.custom.ethereum.ICOBenchAPIs

case class BenchResult(
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
