package tcs.examples.ethereum

import org.web3j.protocol.Web3j
import tcs.blockchain.ethereum.EthereumBlockchain
import tcs.custom.bitcoin.Exchanges


object Tries {
  def main(args: Array[String]): Unit = {
    println(Exchanges.getWallets("Bittrex.com", 10012))
  }
}
