package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Address, Sha256Hash, TransactionOutput}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}
import org.bitcoinj.script.Script

import scala.collection.mutable

/**
  * Defines a transaction output of the Bitcoin blockchain.
  *
  * @param value Output value (in Satoshy).
  * @param index Index of the output (w.r.t. the transaction containing this output).
  * @param transOut Output script.
  */
class BitcoinOutput(
                     val index: Int,
                     val value: Long,
                     val transOut: TransactionOutput) {

  def outScript = {
    try{
      transOut.getScriptPubKey
    } catch {
      case _: Throwable => new Script(new Array[Byte](0))
    }
  }


  /**
    * String representation of a BitcoinOutput.
    *
    * @return String representation of a BitcoinOutput.
    */
  override def toString(): String =
    index + " " + value + " " + transOut


  /**
    * True if the output script uses the OP_RETURN operator.
    *
    * @return True when OP_RETURN is used.
    */
  def isOpreturn(): Boolean = new Script(transOut.getOutPointFor.getConnectedPubKeyScript).isOpReturn


  /**
    * Returns the appended metadata if the output script uses the OP_RETURN operator.
    *
    * @return Either the metadata or null.
    */
  def getMetadata(): String =
    if (!isOpreturn) null else {
      var v1: Integer = transOut.toString.indexOf("[");
      var v2: Integer = transOut.toString.indexOf("]");
      if ((v1 == -1) || (v2 == -1)) {
        //TODO decide when it should return an empty string
        return transOut.toString
      }
      else
        return transOut.toString.substring(v1 + 1, v2)
    }


  /**
    * Returns the address that received the funds of this transaction
    * output, when the information is available.
    *
    * @param network Network settings.
    * @return Either the recipient address or None.
    */
  def getAddress(network: Network): Option[Address] = {



    val param = network match {
      case MainNet => MainNetParams.get
      case TestNet => TestNet3Params.get
    }

    try {

      Some(transOut.getScriptPubKey.getToAddress(param))

    } catch {
      case _: Throwable => None
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
        output.getOutPointFor.getConnectedOutput
      } catch {
        case e: Exception => null
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
      output.getOutPointFor.getConnectedOutput
    } catch {
        case e: Exception => null
      })
  }
}