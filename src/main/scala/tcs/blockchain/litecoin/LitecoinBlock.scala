package tcs.blockchain.litecoin

import java.util.Date

import org.litecoinj.core.{Block, Sha256Hash}
import tcs.blockchain.{Block => TCSBlock}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Defines a block of the Litecoin blockchain.
  *
  * @param hash Block hash
  * @param date Date in which the block was published
  * @param blockSize Size of the block
  * @param height Height of the block
  * @param litecoinTxs List of transactions appended to the block
  */
class LitecoinBlock(
                    val hash: Sha256Hash,
                    val date: Date,
                    val blockSize: Int,
                    val height: Long,
                    val litecoinTxs: List[LitecoinTransaction]) extends TCSBlock{


  /**
    * Returns a String representation of the block
    *
    * @return String representation
    */
  override def toString(): String = {
    val stringTransactions: String = "[ " + litecoinTxs.map(tx => tx.toString() + " ") + "]"
    return hash + " " + date + " " + blockSize + " " + height + " " + stringTransactions
  }
}

/**
  * Factories for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
  */
object LitecoinBlock {


  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
    * Creates a new block given its LitecoinJ representation and its height
    * in the blockchain (height is not specified in the LitecoinJ objects).
    * Input values of each appended transaction will be set to 0.
    *
    * @param block LitecoinJ representation of the block
    * @param height Height of the block given in the previous parameter
    * @return A new LitecoinBlock
    */
  def factory(block: Block, height: Long): LitecoinBlock = {
    val transactions: List[LitecoinTransaction] = block.getTransactions.asScala.map(tx => LitecoinTransaction.factory(tx)).toList

    return new LitecoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }


  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
    * Creates a new block given its LitecoinJ representation and its height
    * in the blockchain (height is not specified in the BitcoinJ objects).
    * Input values of each appended transaction will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param block LitecoinJ representation of the block
    * @param height Height of the block given in the previous parameter
    * @param UTXOmap Unspent transaction outputs map
    * @return A new LitecoinBlock
    */
  def factory(block: Block, height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): LitecoinBlock = {
    val transactions: List[LitecoinTransaction] = block.getTransactions.asScala.map(tx => LitecoinTransaction.factory(tx, UTXOmap, height)).toList

    return new LitecoinBlock(block.getHash, block.getTime, block.getMessageSize, height, transactions)
  }


  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
    * Creates a new block given its LitecoinJ representation.
    * Block height will be set to 0 since is not provided in the LitecoinJ block provided.
    * Input values of each appended transaction will be set to 0.
    *
    * @param block LitecoinJ representation of the block
    * @return A new LitecoinBlock
    */
  def factory(block: Block): LitecoinBlock = {
    val transactions: List[LitecoinTransaction] = block.getTransactions.asScala.map(tx => LitecoinTransaction.factory(tx)).toList

    return new LitecoinBlock(block.getHash, block.getTime, block.getMessageSize, 0, transactions)
  }
}


