package it.unica.blockchain.externaldata.ICOAPIs.ICOBenchAPIs

/**
  * Pojo classes for icos/filters API
  */
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
