package it.unica.blockchain.blockchains.ethereum


/**
  * Framework settings for the Ethereum blockchain.
  *
  * @param url Address where parity is listening
  * @param retrieveVerifiedContracts If true the framework fetches verified contracts from Etherscan.io
  */
class EthereumSettings (
                         val url : String,
                         val retrieveVerifiedContracts : Boolean = false
                       ){
}
