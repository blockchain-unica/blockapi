package tcs.examples.ethereum

import tcs.custom.ethereum.ICO


object Tries {
  def main(args: Array[String]): Unit = {
    val gameFlip = new ICO("GameFlip")
    val neuromation = new ICO("Neuromation")
    println(gameFlip.getHypeScore())
    println(neuromation.getInvestmentRating())
    println(gameFlip.getRiskScore())
    println(neuromation.getHypeScore())
    println(gameFlip.getInvestmentRating())
    println(neuromation.getRiskScore())
  }
}
