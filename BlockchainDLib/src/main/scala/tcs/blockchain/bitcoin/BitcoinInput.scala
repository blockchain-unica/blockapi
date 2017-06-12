package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, TransactionInput}

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinInput(
                    val txHash: Sha256Hash,
                    val index: Integer,
                    val value: Long,
                    val inScript: BitcoinScript){
}

object BitcoinInput {
  def factory(input: TransactionInput): BitcoinInput = {
    new BitcoinInput(input.getHash, input.getParentTransaction.getInputs.indexOf(input), input.getValue.longValue(), new BitcoinScript(input.getScriptBytes))
  }
}