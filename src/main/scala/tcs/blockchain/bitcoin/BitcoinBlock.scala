package tcs.blockchain.bitcoin

import java.util.Date

import org.bitcoinj.core.{Block, Sha256Hash}
import tcs.blockchain.Block

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
                    val bitcoinTxs: List[BitcoinTransaction]) extends Block{


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
  * Factories for [[tcs.blockchain.bitcoin.BitcoinBlock]] instances.
  */
object BitcoinBlock {


  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinBlock]] instances.
    * Creates a new block given its BitcoinJ representation and its height
    * in the blockchain (height is not specified in the BitcoinJ objects).
    * Input values of each appended transaction will be set to 0.
    *
    * @param block BitcoinJ representation of the block
    * @param height Height of the block given in the previous parameter
    * @return A new BitcoinBlock
    */
  def factory(block: Block, height: Long): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }


  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinBlock]] instances.
    * Creates a new block given its BitcoinJ representation and its height
    * in the blockchain (height is not specified in the BitcoinJ objects).
    * Input values of each appended transaction will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param block BitcoinJ representation of the block
    * @param height Height of the block given in the previous parameter
    * @param UTXOmap Unspent transaction outputs map
    * @return A new BitcoinBlock
    */
  def factory(block: Block, height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx, UTXOmap, height)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }


  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinBlock]] instances.
    * Creates a new block given its BitcoinJ representation.
    * Block height will be set to 0 since is not provided in the BitcoinJ block provided.
    * Input values of each appended transaction will be set to 0.
    *
    * @param block BitcoinJ representation of the block
    * @return A new BitcoinBlock
    */
  def factory(block: Block): BitcoinBlock = {
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, 0, transactions)
  }
}