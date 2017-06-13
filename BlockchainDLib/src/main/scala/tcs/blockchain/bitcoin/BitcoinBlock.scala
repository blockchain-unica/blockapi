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
                    val blockSize: Int,
                    val height: Long,
                    val bitcoinTxs: List[BitcoinTransaction]){
}

object BitcoinBlock {
  def factory(block: Block, height: Long): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map( tx => BitcoinTransaction.factory(tx)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }

  def factory(block: Block): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map( tx => BitcoinTransaction.factory(tx)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, 0, transactions)
  }
}