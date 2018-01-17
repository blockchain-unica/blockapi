package tcs.examples.ethereum

import tcs.custom.ethereum.ICO


object Tries {
  def main(args: Array[String]): Unit = {
    val zerox = new ICO("0x")
    val gnagno = zerox.getContractAddress
    val address = gnagno
    println(address)
  }
}
