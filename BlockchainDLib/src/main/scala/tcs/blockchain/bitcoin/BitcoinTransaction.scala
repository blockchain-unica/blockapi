package tcs.blockchain.bitcoin

import org.bitcoinj.core.{Sha256Hash, Transaction}
import collection.JavaConverters._

/**
  * Created by Livio on 12/06/2017.
  */
class BitcoinTransaction(
                          val blockHash: Sha256Hash,
                          val hash: Sha256Hash,
                          val txSize: Integer,
                          val inputs: List[BitcoinInput],
                          val outputs: List[BitcoinOutput]){
}

object BitcoinTransaction {
  def factory(tx: Transaction, blockHash: Sha256Hash): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map( i => BitcoinInput.factory(i)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map (o => BitcoinOutput.factory(o)).toList

    // Is getMessageSize correct?
    return new BitcoinTransaction(blockHash, tx.getHash, tx.getMessageSize , inputs, outputs)
  }
}