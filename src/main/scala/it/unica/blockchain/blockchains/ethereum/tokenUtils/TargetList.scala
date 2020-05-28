package it.unica.blockchain.blockchains.ethereum.tokenUtils

import it.unica.blockchain.blockchains.ethereum.EthereumAddress

import scala.collection.mutable.ListBuffer

/** This object mantains a list of target addresses during the analisys.
  * If you want to check informations about only one or some addresses
  * into the blockchain you can add them into this object.
  */

object TargetList {
  private val listTarget: ListBuffer[String] = ListBuffer()

  def getList(): ListBuffer[String] ={
    listTarget
  }

  def add(address: EthereumAddress){
    listTarget += address.address
  }

  def contains(address : EthereumAddress): Boolean ={
    listTarget.contains(address.address)
  }
}
