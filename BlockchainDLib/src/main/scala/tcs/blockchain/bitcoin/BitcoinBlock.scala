package tcs.blockchain.bitcoin

import java.util.Date

import org.bitcoinj.core.{Block, Sha256Hash, Transaction}
import collection.JavaConverters._

/**
  * Created by stefano on 08/06/17.
  */
class BitcoinBlock(
                    val hash: Sha256Hash,
                    val date: Date,
                    val blockSize: Integer,
                    val bitcoinTxs: List[BitcoinTransaction]){
}

object BitcoinBlock {
  def factory(block: Block): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map( tx => BitcoinTransaction.factory(tx, block.getHash)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, transactions)
  }
}