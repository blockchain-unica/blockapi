package tcs.blockchain.bitcoin


import org.bitcoinj.core.{Sha256Hash, TransactionOutput}

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinOutput(
                     val txHash: Sha256Hash,
                     val index: Integer,
                     val value: Long,
                     val outScript: BitcoinScript){
}

object BitcoinOutput {
  def factory(output: TransactionOutput): BitcoinOutput = {
    new BitcoinOutput(output.getHash, output.getIndex, output.getValue.longValue(), new BitcoinScript(output.getScriptBytes))
  }
}