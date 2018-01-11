package tcs.examples.ethereum

import org.web3j.protocol.Web3j
import tcs.blockchain.ethereum.EthereumBlockchain
import tcs.custom.ethereum.Exchanges


object Tries {
  def main(args: Array[String]): Unit = {
    val infuraBlockchain = new EthereumBlockchain("http://localhost:8545/")
      .setStart(1).setStep(100)
    infuraBlockchain.foreach((block) => {
      println(block.hash + ";" + block.number + ";" + block.transactions.length)
    })
  }
}
