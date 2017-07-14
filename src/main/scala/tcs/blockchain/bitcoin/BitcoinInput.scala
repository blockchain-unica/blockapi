package tcs.blockchain.bitcoin

import javax.script.ScriptException

import org.bitcoinj.core.{Address, Sha256Hash, TransactionInput, TransactionOutput}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}

import scala.collection.mutable

/**
  * Defines a transaction input of the Bitcoin blockchain.
  *
  * @param redeemedTxHash Hash of the transaction containing the redeemed output.
  * @param value Input value (in Satoshy).
  * @param redeemedOutIndex Index of the output redeemed (w.r.t. the transaction containing the output);
  *                         null if the enclosing transaction is coinbase.
  * @param isCoinbase True if the enclosing transaction is coninbase.
  * @param inScript Input script.
  */
class BitcoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val value: Long,
                    val redeemedOutIndex: Int,
                    val isCoinbase: Boolean,
                    val inScript: BitcoinScript) {


  /**
    * String representation of a BitcoinInput.
    *
    * @return String representation of a BitcoinInput.
    */
  override def toString(): String =
    redeemedTxHash + " " + value + " " + redeemedOutIndex + " " + isCoinbase + " " + inScript


  /**
    * Returns the address that received the funds of this transaction
    * input, when the information is available.
    *
    * @param network Network settings.
    * @return Either the recipient address or None.
    */
  def getAddress(network: Network): Option[Address] = {
    try {
      if (inScript.isPayToScriptHash || inScript.isSentToAddress) {
        network match {
          case MainNet => Some(inScript.getToAddress(MainNetParams.get))
          case TestNet => Some(inScript.getToAddress(TestNet3Params.get))
        }
      } else None
    } catch {
      case _: ScriptException => None
    }
  }
}


/**
  * Factories for [[tcs.blockchain.bitcoin.BitcoinInput]] instances.
  */
object BitcoinInput {

  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinInput]] instances.
    * Creates a new input given its BitcoinJ representation.
    * The value will be set to 0.
    *
    * @param input BitcoinJ representation of the input.
    * @return A new BitcoinInput.
    */
  def factory(input: TransactionInput): BitcoinInput = {
    new BitcoinInput(if (input.getConnectedOutput != null) input.getConnectedOutput.getParentTransactionHash else null,
      0,
      input.getParentTransaction.getInputs.indexOf(input),
      if (input.getConnectedOutput == null) true else false,

      try {
        new BitcoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      }
    )
  }


  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinInput]] instances.
    * Creates a new input given its BitcoinJ representation.
    * The value will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param input BitcoinJ representation of the input.
    * @param UTXOmap Unspent transaction outputs map.
    * @param blockHeight Height of the block including the enclosing transaction.
    * @param outputs List of outputs of the enclosing transaction.
    * @return A new BitcoinInput.
    */
  def factory(input: TransactionInput, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long, outputs: List[TransactionOutput]): BitcoinInput = {
    val value = UTXOmap.get((input.getOutpoint.getHash, input.getOutpoint.getIndex)) match {
      // The input value corresponds to the connected output: retrieves it from the map
      case Some(l) => {
        UTXOmap.remove((input.getOutpoint.getHash, input.getOutpoint.getIndex))
        l
      }
      // If the map does not contains the value, then the enclosing transaction should be coinbase.
      case None =>
        if (!input.isCoinBase)
          0 // Error case
        else {
          sum(outputs)
        }
    }

    // Create the new BitcoinInput
    new BitcoinInput(if (input.getConnectedOutput != null) input.getOutpoint.getHash else null,
      value,
      input.getOutpoint.getIndex.toInt,
      if (input.getConnectedOutput == null) true else false,
      try {
        new BitcoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      }
    )
  }

  private def sum(xs: List[TransactionOutput]): Long = {
    xs.map(_.getValue.longValue()).sum
  }
}