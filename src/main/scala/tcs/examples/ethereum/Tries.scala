package tcs.examples.ethereum

import tcs.custom.ethereum.ICO
import tcs.custom.ethereum.ICOBenchAPIs.ICOBenchAPI


object Tries {
  def main(args: Array[String]): Unit = {
    val gameFlip = new ICO("GameFlip")
    val icosPage = ICOBenchAPI.getIco(1)
    print(icosPage)
  }
}
