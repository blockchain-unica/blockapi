package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, TransactionInput}

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val value: Long,
                    val redeemedOutIndex: Integer,
                    val isCoinbase: Boolean,
                    val inScript: BitcoinScript){

  override def toString(): String =
    redeemedTxHash + " " + value + " "
    + redeemedOutIndex + " " + isCoinbase + " " + inScript
}

object BitcoinInput {
  def factory(input: TransactionInput): BitcoinInput = {
    new BitcoinInput(if (input.getConnectedOutput!=null) input.getConnectedOutput.getParentTransactionHash else null,
                    0,
                    input.getParentTransaction.getInputs.indexOf(input),
                    if (input.getConnectedOutput==null) true else false,
                    new BitcoinScript(input.getScriptBytes))
  }
}