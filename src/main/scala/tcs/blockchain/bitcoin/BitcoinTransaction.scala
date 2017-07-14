package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, Transaction}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Defines a transaction of the Bitcoin blockchain.
  *
  * @param hash Transaction hash
  * @param txSize Size of the transaction
  * @param inputs List of transaction inputs
  * @param outputs List of transaction outputs
  */
class BitcoinTransaction(
                          val hash: Sha256Hash,
                          val txSize: Int,
                          val inputs: List[BitcoinInput],
                          val outputs: List[BitcoinOutput]) {


  /**
    * Returns the sum of all the input values.
    * If a "deep scan" was not performed, each input value is set to 0.
    *
    * @return Sum of all the input values
    */
  def getInputsSum(): Long = {
    inputs.map(input => input.value).reduce(_ + _)
  }


  /**
    * Returns the sum of all the output values.
    *
    * @return Sum of all the output values
    */
  def getOutputsSum(): Long = {
    outputs.map(output => output.value).reduce(_ + _)
  }


  /**
    * Returns a string representation of the transaction,
    * including hash, size, list of inputs, and list of outputs.
    *
    * @return String representation of the object.
    */
  override def toString(): String = {
    val stringInputs: String = "[ " + inputs.map(i => i.toString() + " ") + "]"
    val stringOutputs: String = "[ " + outputs.map(o => o.toString() + " ") + "]"

    return hash + " " + txSize + " " + stringInputs + " " + stringOutputs
  }
}


/**
  * Factories for [[tcs.blockchain.bitcoin.BitcoinTransaction]] instances.
  */
object BitcoinTransaction {

  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended BitcoinInput will be set to 0.
    *
    * @param tx BitcoinJ representation of the transaction
    * @return A new BitcoinTransaction
    */
  def factory(tx: Transaction): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash, tx.getMessageSize, inputs, outputs)
  }

  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended BitcoinInput will be set to the correct value
    * by exploiting the UTXOmap provided.
    *
    * @param tx BitcoinJ representation of the transaction
    * @param UTXOmap Unspent transaction outputs map
    * @param blockHeight Height of the enclosing block
    * @return A new BitcoinTransaction
    */
  def factory(tx: Transaction, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i, UTXOmap, blockHeight, tx.getOutputs.asScala.toList)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o, tx.getHash, UTXOmap)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash, tx.getMessageSize, inputs, outputs)
  }
}