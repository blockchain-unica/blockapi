package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, TransactionInput}

import scala.collection.mutable

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val value: Long,
                    val redeemedOutIndex: Int,
                    val isCoinbase: Boolean,
                    val inScript: BitcoinScript) {

  override def toString(): String =
    redeemedTxHash + " " + value + " " + redeemedOutIndex + " " + isCoinbase + " " + inScript
}

object BitcoinInput {
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

  def factory(input: TransactionInput, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long): BitcoinInput = {
    val value = UTXOmap.get((input.getOutpoint.getHash, input.getOutpoint.getIndex)) match {
      case Some(l) => {
        UTXOmap.remove((input.getOutpoint.getHash, input.getOutpoint.getIndex))
        l
      }
      case None =>
        if(! input.isCoinBase)
          0 // Error case
        else {
          500000000 / (2 ^ (blockHeight % 210000).toInt)
        }
    }


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

}