package it.unica.blockchain.blockchains

import java.util.Date

trait Block{

  val hash : String
  val height : BigInt
  val date : Date
  val size : BigInt
  val txs: List[Transaction]

  def getMiningPool: String
}