package tcs.blockchain.litecoin

import java.util.Date

import org.litecoinj.core.{Block, Sha256Hash}
import tcs.blockchain.{Block => TCSBlock}
import tcs.externaldata.miningpools.MiningPools

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Defines a block of the Litecoin blockchain.
  *
  * @param hash Block hash
  * @param height Height of the block
  * @param date Date in which the block was published
  * @param size Size of the block
  * @param txs List of transactions appended to the block
  */

class LitecoinBlock(
                    override val hash: String,
                    override val height: BigInt,
                    override val date: Date,
                    override val size: BigInt,

                    val txs: List[LitecoinTransaction]) extends TCSBlock {


  /**
    * Returns a String representation of the block
    *
    * @return String representation
    */
  override def toString(): String = {
    val stringTransactions: String = "[ " + txs.map(tx => tx.toString() + " ") + "]"
    return hash + " " + date + " " + size + " " + height + " " + stringTransactions
  }

  /**
    * Returns the name of the mining pool who mined the block.
    *
    * @return Mining pool
    */
  override def getMiningPool(): String = {
    MiningPools.getLitecoinPool(txs.head)
  }
}


/**
  * Factories for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
  */
object LitecoinBlock {


  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
    * Creates a new block given its LitecoinJ representation and its height
    * in the blockchain (height is not specified in the BitcoinJ objects).
    * Input values of each appended transaction will be set to 0.
    *
    * @param block LitecoinJ representation of the block
    * @param height Height of the block given in the previous parameter
    * @return A new LitecoinBlock
    */
  def factory(block: Block, height: Long): LitecoinBlock = {
    val transactions: List[LitecoinTransaction] = block.getTransactions.asScala.map(tx => LitecoinTransaction.factory(tx, block.getTime)).toList

    return new LitecoinBlock(block.getHash.toString, height, block.getTime, block.getMessageSize, transactions)
  }



  /**
    * Factory for [[tcs.blockchain.litecoin.LitecoinBlock]] instances.
    * Creates a new block given its LitecoinJ representation and its height
    * in the blockchain (height is not specified in the LitecoinJ objects).
    * Input values of each appended transaction will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param block LitecoinJ representation of the block
    * @param height Height of the block given in the previous parameter
    * @param UTXOmap Unspent transaction outputs map
    * @return A new LitecoinBlock
    */
  def factory(block: Block, height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): LitecoinBlock = {
    val transactions: List[LitecoinTransaction] = block.getTransactions.asScala.map(tx => LitecoinTransaction.factory(tx, block.getTime, UTXOmap, height)).toList

    return new LitecoinBlock(block.getHash.toString, height, block.getTime, block.getMessageSize, transactions)
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
    val transactions: List[LitecoinTransaction] = block.getTransactions.asScala.map(tx => LitecoinTransaction.factory(tx, block.getTime)).toList

    return new LitecoinBlock(block.getHash.toString, 0, block.getTime, block.getMessageSize, transactions)
  }
}