package tcs.examples.ethereum

import tcs.custom.ethereum.ICO


object Tries {
  def main(args: Array[String]): Unit = {
    val zerox = new ICO("0x")
    val gnagno = zerox.getAddressBalance("0x25Ce37C57152DD7DfbE55E8387CfE2C2B0Da1924")
    print(gnagno)
  }
}
