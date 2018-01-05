package tcs.examples.ethereum

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumBlock

object Tries {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib
                      .getEthereumBlockchain("http://localhost:8545")
                      .setStart(500000).setEnd(500500).setStep(1)
    blockchain.foreach((block: EthereumBlock) => {
      println(block.number + " " + block.internalTransactions)
    })
  }
}
