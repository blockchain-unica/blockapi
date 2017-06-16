package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, Transaction}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Livio on 12/06/2017.
  */
class BitcoinTransaction(
                          val hash: Sha256Hash,
                          val txSize: Int,
                          val inputs: List[BitcoinInput],
                          val outputs: List[BitcoinOutput]) {

  def getInputsSum(): Long = {
    inputs.map(input => input.value).reduce(_ + _)
  }

  def getOutputsSum(): Long = {
    outputs.map(output => output.value).reduce(_ + _)
  }

  override def toString(): String = {
    val stringInputs: String = "[ " + inputs.map(i => i.toString() + " ") + "]"
    val stringOutputs: String = "[ " + outputs.map(o => o.toString() + " ") + "]"

    return hash + " " + txSize + " " + stringInputs + " " + stringOutputs
  }
}

object BitcoinTransaction {
  def factory(tx: Transaction): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o)).toList

    // Is getMessageSize correct?
    return new BitcoinTransaction(tx.getHash, tx.getMessageSize, inputs, outputs)
  }

  def factory(tx: Transaction, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i, UTXOmap, blockHeight)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o, tx.getHash, UTXOmap)).toList

    // Is getMessageSize correct?
    return new BitcoinTransaction(tx.getHash, tx.getMessageSize, inputs, outputs)
  }
}