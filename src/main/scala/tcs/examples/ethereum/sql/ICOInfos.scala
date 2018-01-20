package tcs.examples.ethereum.sql

import tcs.blockchain.BlockchainLib
import tcs.custom.ethereum.ICO
import tcs.db.{DatabaseSettings, MySQL}


object ICOInfos {
  def main(args: Array[String]): Unit = {
    val ico = new ICO("0x9a642d6b3368ddc662CA244bAdf32cDA716005BC")
    println(ico.getUSDPrice) //TODO fix these calls
    println(ico.getETHPrice)
    println(ico.getBTCPrice)
    println(ico.getBlockchain)
    //val blockchain = BlockchainLib.getEthereumBlockchain("http://localhost:8545")
    //  .setStart(0).setEnd(1500000)
    //val pg = new DatabaseSettings("ethereum", MySQL, "postgres")
  }
}
