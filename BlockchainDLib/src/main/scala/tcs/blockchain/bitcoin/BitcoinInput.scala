package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, TransactionInput}

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val redeemedOutIndex: Integer,
                    val value: Long,
                    val inScript: BitcoinScript,
                    val isCoinbase: Boolean){
}

object BitcoinInput {
  def factory(input: TransactionInput): BitcoinInput = {
    new BitcoinInput(if (input.getConnectedOutput!=null) input.getConnectedOutput.getParentTransactionHash else null,
                    input.getParentTransaction.getInputs.indexOf(input),
                    0,
                    new BitcoinScript(input.getScriptBytes),
                    if (input.getConnectedOutput==null) true else false)
  }
}