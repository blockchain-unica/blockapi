package tcs.examples.ethereum.sql

import tcs.blockchain.BlockchainLib
import tcs.custom.ethereum.ICO
import tcs.db.{DatabaseSettings, MySQL}


object ICOInfos {
  def main(args: Array[String]): Unit = {
    val ico = new ICO("0xe41d2489571d322189246dafa5ebde1f4699f498")
    println(ico.getName)
    println(ico.getSymbol)
    println(ico.getUSDPrice)
    println(ico.getETHPrice)
    println(ico.getBTCPrice)
    println(ico.getMarketCap)
    //val blockchain = BlockchainLib.getEthereumBlockchain("http://localhost:8545")
    //  .setStart(0).setEnd(1500000)
    //val pg = new DatabaseSettings("ethereum", MySQL, "postgres")
  }
}
