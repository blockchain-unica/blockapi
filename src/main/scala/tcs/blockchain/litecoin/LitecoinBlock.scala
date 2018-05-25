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
  //TODO: inserire i mining pools per litecoin
  //https://bitmakler.net/mining_Litecoin-LTC__pools
  //

  override def getMiningPool(): String ={
    val firstTransaction: LitecoinTransaction = txs.head
    var pool: String = "Unknown"
    //first implementation based on bitcoin
    if(firstTransaction.inputs.head.isCoinbase) {
      val programByte: Array[Byte] = firstTransaction.inputs.head.inScript.getProgram()
      if(programByte != null) {
        val hex: String = programByte.map("%02x".format(_)).mkString
        if(hex != "") {
          pool = getPoolByHexCode(hex)
        }
      }
    }
    return pool
  }

  //TODO: mining pools by hex per litecoin (verificarli)
  private def getPoolByHexCode(hex: String): String ={
    //AntPool, F2Pool are LTC mining pools too.
    //Trying to find pool codes for LTC.
    if(hex.contains("416e74506f6f6c3")) return "AntPool"

    if(hex.contains("777868")) return "F2Pool"
    if(hex.contains("66326261636b7570")) return "F2Pool"
    if(hex.contains("68663235")) return "F2Pool"
    if(hex.contains("73796a756e303031")) return "F2Pool"
    if(hex.contains("716c7339")) return "F2Pool"
    if(hex.contains("687578696e6767616f7a68616f")) return "F2Pool"

    return "Unknown"
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

