package tcs.blockchain

import java.util.Date

trait Transaction{

  val hash : String
  val date: Date
}