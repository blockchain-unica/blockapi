package tcs.pojos

/**
  * Created by Ferruvich on 03/08/2017.
  */
class Action(
              callType: String,
              from: String,
              gas: String,
              input: String,
              init: String,
              to: String,
              value: String,
              address: String,
              balance: String,
              refundAddress: String
              ) {

  def this() = {
    this("", "", "", "", "", "", "", "", "", "")
  }

  def this(from: String, gas: String, init: String, value: String) = {
    this("", from, gas, "", init, "" , value, "", "", "")
  }

  def this(callType: String, from: String, gas: String, input: String, to: String, value: String) = {
    this(callType, from, gas, input, "", to, value, "", "", "")
  }

  //Suicide type
  def this(address: String, balance: String, refundAddress: String) ={
    this("", "", "", "", "", "", "", address, balance, refundAddress)
  }

  def getCallType: String = {
    this.callType
  }

  def getFrom: String = {
    this.from
  }

  def getGas: String = {
    this.gas
  }

  def getInput: String = {
    this.input
  }

  def getInit: String = {
    this.init
  }

  def getTo: String = {
    this.to
  }

  def getValue: String = {
    this.value
  }

  def getAddress: String = {
    this.address
  }

  def getBalance: String = {
    this.balance
  }

  def getRefundAddress: String = {
    this.refundAddress
  }
}