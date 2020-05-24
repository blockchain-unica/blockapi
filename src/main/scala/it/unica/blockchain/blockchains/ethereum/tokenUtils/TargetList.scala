package it.unica.blockchain.blockchains.ethereum.tokenUtils

import scala.collection.mutable.ListBuffer

object TargetList {
  private val listTarget: ListBuffer[String] = ListBuffer()

  def getList(): ListBuffer[String] ={
    listTarget
  }

  def add(address: String){
    listTarget += address
  }

  def contains(address : String): Boolean ={
    listTarget.contains(address)
  }
}
