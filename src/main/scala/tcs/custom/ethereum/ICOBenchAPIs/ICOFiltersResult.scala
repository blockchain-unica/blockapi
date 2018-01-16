package tcs.custom.ethereum.ICOBenchAPIs

case class ICOFiltersResult(
                             categories: Array[Category],
                             platforms: Array[Name],
                             accepting: Array[Name],
                             trading: Array[Name],
                             countries: Array[Name]
                           )

case class Name(
                 name: String
               )
