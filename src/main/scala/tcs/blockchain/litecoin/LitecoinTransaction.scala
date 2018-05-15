package tcs.blockchain.litecoin

import org.litecoinj.core.{Sha256Hash, Transaction}
import tcs.blockchain.{Transaction => TCSTransaction}

import scala.collection.JavaConverters._
import scala.collection.mutable
/**
  * Defines a transaction of the Litecoin blockchain.
  *
  * @param hash Transaction hash
  * @param txSize Size of the transaction
  * @param inputs List of transaction inputs
  * @param outputs List of transaction outputs
  */
class LitecoinTransaction(
                          val hash: Sha256Hash, /*scrypt?*/
                          val txSize: Int,
                          val inputs: List[LitecoinInput],
                          val outputs: List[LitecoinOutput],
                          val lock_time: Long) extends TCSTransaction{


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
    * Returns the transaction lock time.
    *
    * @return Transaction lock time
    */
  def getLockTime(): Long = {
    lock_time
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
  * Factories for [[tcs.blockchain.litecoin.LitecoinTransaction]] instances.
  */
object LitecoinTransaction {

  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinTransaction]] instances.
    * Creates a new transaction given its LitecoinJ representation.
    * Values of each appended LitecoinInput will be set to 0.
    *
    * @param tx LitecoinJ representation of the transaction
    * @return A new LitecoinTransaction
    */
  def factory(tx: Transaction): LitecoinTransaction = {
    val inputs: List[LitecoinInput] = tx.getInputs.asScala.map(i => LitecoinInput.factory(i)).toList
    val outputs: List[LitecoinOutput] = tx.getOutputs.asScala.map(o => LitecoinOutput.factory(o)).toList

    // TODO: Test getMessageSize
    return new LitecoinTransaction(tx.getHash, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }

  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended LitecoinInput will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param tx LitecoinJ representation of the transaction
    * @param UTXOmap Unspent transaction outputs map
    * @param blockHeight Height of the enclosing block
    * @return A new LitecoinTransaction
    */
  def factory(tx: Transaction, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long): LitecoinTransaction = {
    val inputs: List[LitecoinInput] = tx.getInputs.asScala.map(i => LitecoinInput.factory(i, UTXOmap, blockHeight, tx.getOutputs.asScala.toList)).toList
    val outputs: List[LitecoinOutput] = tx.getOutputs.asScala.map(o => LitecoinOutput.factory(o, tx.getHash, UTXOmap)).toList

    // TODO: Test getMessageSize
    return new LitecoinTransaction(tx.getHash, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }
}