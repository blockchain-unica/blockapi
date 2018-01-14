package tcs.examples.ethereum

import tcs.custom.ethereum.ICO


object Tries {
  def main(args: Array[String]): Unit = {
    val gameFlip = new ICO("GameFlip")
    gameFlip.getContractAddress
  }
}
