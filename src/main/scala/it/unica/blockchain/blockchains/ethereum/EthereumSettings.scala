package it.unica.blockchain.blockchains.ethereum


/**
  * Framework settings for the Ethereum blockchain.
  *
  * @param url Address where parity is listening
  * @param retrieveVerifiedContracts If true the framework fetches verified contracts from Etherscan.io
  * @param searchForTokens If true the framework checks if transactions contains function calls and if contracts are tokens
  */
class EthereumSettings (
                         val url : String,
                         val retrieveVerifiedContracts : Boolean = false,
                         val searchForTokens : Boolean = false
                       ){
}
