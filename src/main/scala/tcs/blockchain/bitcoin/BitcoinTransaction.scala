package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, Transaction}
import org.bitcoinj.script.Script.ScriptType
import tcs.blockchain.{Transaction => TCSTransaction}
import java.util.Date

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
                          override val hash: String,
                          override val date: Date,

                          val txSize: Int,
                          val inputs: List[BitcoinInput],
                          val outputs: List[BitcoinOutput],
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


  /**
    * Returns a boolean which states if the transaction is standard or not
    * @return True if the transaction is standard, false otherwise
    */
  def isStandard : Boolean = {
    transactionType match {
      case TxType.TX_STANDARD => hasStandardConditions
      case _ => false
    }
  }


  /**
    * Iterates through the output script list and checks if they are standard or not
    * @return the related type of the enumeration
    */
  def transactionType : TxType.Value = {
    outputs.foreach(out => {
      if(
        out.transOut.getScriptPubKey.getScriptType == ScriptType.P2SH ||
        out.transOut.getScriptPubKey.getScriptType == ScriptType.P2PKH ||
        out.transOut.getScriptPubKey.getScriptType == ScriptType.PUB_KEY ||
        out.transOut.getScriptPubKey.isSentToMultiSig ||
        out.transOut.getScriptPubKey.isOpReturn)
        { return TxType.TX_STANDARD }

      return TxType.TX_NONSTANDARD

    })

    return TxType.TX_NOTYPE
  }


  /**
    * Checks the size of a transaction
    * @return true if the size is less than 100,000 bytes, false otherwise
    */
  private def hasStandardTransactionSize : Boolean = {
    return txSize < 100000
  }


  /**
    * Checks the size of each script
    * @return true if the size is less than 1,650 bytes, false otherwise
    */
  private def hasStandardScriptSize : Boolean =  {
    outputs.foreach(out => {
      val size = out.outScript.getProgram.length
      if(size > 1650){
        return false
      }
    })
    return true
  }


  /**
    * Checks the number of signatures required
    * @return true if the number of signatures is less or equal than 3, false otherwise
    */
  private def hasStandardSignatures : Boolean =  {
    outputs.foreach(out => {
      if(out.transOut.getScriptPubKey.isSentToMultiSig){
        val signatures = out.transOut.getScriptPubKey.getNumberOfSignaturesRequiredToSpend
        if(signatures > 3)  {
          return false
        }
      }
    })
    return true
  }


  /**
    * Checks if each input script pushes only data and not opcodes to the evaluation stack
    * @return true if pushes only data, false otherwise
    */
  private def hasStandardPushData : Boolean =  {
    for(input <- inputs) {
      val chunks = input.inScript.getChunks.asScala
      chunks.foreach(chunk => {
        if (!input.isCoinbase && !chunk.isPushData) {
          return false
        }
      })
    }
    return true
  }


  /**
    * Checks if all conditions are evaluated true
    * @return true if all conditions are respected, false otherwise
    */
  private def hasStandardConditions : Boolean = {
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy")
    if(date.after(format.parse("27-09-2014"))){
      return hasStandardTransactionSize && hasStandardSignatures && hasStandardScriptSize && hasStandardPushData
    }
    return true
  }
}

/**
  * Enumeration of all possible transaction types
  */
object TxType extends Enumeration {
  type TxType = Value
  val TX_NOTYPE,
  TX_STANDARD,
  TX_NONSTANDARD = Value
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
    * @param txDate Date in which the containing block has been published
    * @return A new BitcoinTransaction
    */
  def factory(tx : Transaction, txDate : Date): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash.toString, txDate, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }

  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended BitcoinInput will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param tx BitcoinJ representation of the transaction
    * @param txDate Date in which the containing block has been published
    * @param UTXOmap Unspent transaction outputs map
    * @param blockHeight Height of the enclosing block
    * @return A new BitcoinTransaction
    */
  def factory(tx: Transaction, txDate : Date, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i, UTXOmap, blockHeight, tx.getOutputs.asScala.toList)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o, tx.getHash, UTXOmap)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash.toString, txDate, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }
}