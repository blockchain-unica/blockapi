package tcs.blockchain.bitcoin


import javax.script.ScriptException

import org.bitcoinj.core.{Address, Sha256Hash, TransactionOutput}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}

import scala.collection.mutable

/**
  * Defines a transaction output of the Bitcoin blockchain.
  *
  * @param value Output value (in Satoshy).
  * @param index Index of the output (w.r.t. the transaction containing this output).
  * @param outScript Output script.
  */
class BitcoinOutput(
                     val index: Int,
                     val value: Long,
                     val outScript: BitcoinScript) {


  /**
    * String representation of a BitcoinOutput.
    *
    * @return String representation of a BitcoinOutput.
    */
  override def toString(): String =
    index + " " + value + " " + outScript


  /**
    * True if the output script uses the OP_RETURN operator.
    *
    * @return True when OP_RETURN is used.
    */
  def isOpreturn(): Boolean = outScript.isOpReturn


  /**
    * Returns the appended metadata if the output script uses the OP_RETURN operator.
    *
    * @return Either the metadata or null.
    */
  def getMetadata(): String =
    if (!isOpreturn) null else {
      var v1: Integer = outScript.toString.indexOf("[");
      var v2: Integer = outScript.toString.indexOf("]");
      if ((v1 == -1) || (v2 == -1))
        return ""
      else
        return outScript.toString.substring(v1 + 1, v2)
    }


  /**
    * Returns the address that received the funds of this transaction
    * output, when the information is available.
    *
    * @param network Network settings.
    * @return Either the recipient address or None.
    */
  def getAddress(network: Network): Option[Address] = {
    try {
      if (outScript.isPayToScriptHash || outScript.isSentToAddress) {
        network match {
          case MainNet => Some(outScript.getToAddress(MainNetParams.get))
          case TestNet => Some(outScript.getToAddress(TestNet3Params.get))
        }
      } else None
    } catch {
      case _: ScriptException => None
    }
  }
}


/**
  * Factories for [[tcs.blockchain.bitcoin.BitcoinOutput]] instances.
  */
object BitcoinOutput {

  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinOutput]] instances.
    * Creates a new output given its BitcoinJ representation.
    *
    * @param output BitcoinJ representation of the output.
    * @return A new BitcoinOutput.
    */
  def factory(output: TransactionOutput): BitcoinOutput = {
    new BitcoinOutput(output.getIndex,
      output.getValue.longValue(),

      try {
        new BitcoinScript(output.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      })
  }


  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinOutput]] instances.
    * Creates a new output given its BitcoinJ representation.
    *
    * @param output BitcoinJ representation of the output.
    * @param txHash Hash of the enclosing transaction.
    * @param UTXOmap Unspent transaction outputs map.
    * @return A new BitcoinOutput.
    */
  def factory(output: TransactionOutput, txHash: Sha256Hash, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): BitcoinOutput = {

    // Adds output value to the map, in order to retrieve it when evaluating input values.
    UTXOmap += ((txHash, output.getIndex.toLong) -> output.getValue.longValue())

    new BitcoinOutput(output.getIndex,
      output.getValue.longValue(),
      try {
        new BitcoinScript(output.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      })
  }
}