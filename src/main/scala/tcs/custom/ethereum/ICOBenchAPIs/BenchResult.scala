package tcs.custom.ethereum.ICOBenchAPIs

case class BenchResult(
                      icos: Int,
                      pages: Int,
                      currentPage: Int,
                      results: Array[ICOShortResult]
                      )
