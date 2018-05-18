package tcs.custom.ethereum.ICOAPIs.ethplorerAPIs

/**
  * Pojo class fro Ethplorer
  */
case class EthplorerTokenInfo(
                               address: String,
                               name: String,
                               symbol: String,
                               totalSupply: String,
                               price: Any,
                               owner: String,
                               countOps: Int,
                               transfersCount: Double,
                               totalIn: Double,
                               totalOut: Double,
                               holdersCount: Double,
                               issuancesCount: Double
                             )