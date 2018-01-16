package tcs.examples.ethereum

import tcs.custom.ethereum.ICO
import tcs.custom.ethereum.ICOBenchAPIs.ICOBenchAPI


object Tries {
  def main(args: Array[String]): Unit = {
    val gameFlip = new ICO("GameFlip")
    val icosFilter = ICOBenchAPI.getStats
    print(icosFilter)
  }
}
