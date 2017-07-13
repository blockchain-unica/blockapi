package tcs.blockchain.bitcoin

import java.util.Date

import org.bitcoinj.core.{Block, Sha256Hash}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Defines a block of the Bitcoin blockchain.
  *
  * @param hash Block hash
  * @param date Date in which the block was published
  * @param blockSize Size of the block
  * @param height Height of the block
  * @param bitcoinTxs List of transactions appended to the block
  */
class BitcoinBlock(
                    val hash: Sha256Hash,
                    val date: Date,
                    val blockSize: Int,
                    val height: Long,
                    val bitcoinTxs: List[BitcoinTransaction]) {


  /**
    * Returns a String representation of the block
    *
    * @return String representation
    */
  override def toString(): String = {
    val stringTransactions: String = "[ " + bitcoinTxs.map(tx => tx.toString() + " ") + "]"
    return hash + " " + date + " " + blockSize + " " + height + " " + stringTransactions
  }
}


/**
  * Factory for [[tcs.blockchain.bitcoin.BitcoinBlock]] instances.
  */
object BitcoinBlock {


  def factory(block: Block, height: Long): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }


  def factory(block: Block, height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx, UTXOmap, height)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }


  def factory(block: Block): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, 0, transactions)
  }
}