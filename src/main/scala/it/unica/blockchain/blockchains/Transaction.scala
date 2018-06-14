package it.unica.blockchain.blockchains

import java.util.Date

trait Transaction{

  val hash : String
  val date: Date
}