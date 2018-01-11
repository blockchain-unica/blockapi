package tcs.examples.ethereum

import org.web3j.protocol.Web3j
import org.web3j.protocol.infura.InfuraHttpService
import tcs.blockchain.ethereum.EthereumBlockchain
import tcs.custom.ethereum.Exchanges

object Tries {
  def main(args: Array[String]): Unit = {
    val infuraBlockchain = new EthereumBlockchain("https://mainnet.infura.io/OCPoiiZvFpsPKZcOMGaG")
      .setStart(1).setStep(1)
    infuraBlockchain.foreach((block) => {
      println(block.hash + ";" + block.number)
    })
    //infuraBlockchain = infuraBlockchain.setStart(1).setEnd()
    //println(Exchanges.getExchange("1N52wHoVR79PMDishab2XmRHsbekCdGquK"))
  }
}
