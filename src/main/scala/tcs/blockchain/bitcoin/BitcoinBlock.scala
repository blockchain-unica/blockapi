package tcs.blockchain.bitcoin

import java.util.Date
import javax.xml.bind.DatatypeConverter

import org.bitcoinj.core.{Block, Sha256Hash}
import tcs.blockchain.{Block => TCSBlock}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.matching.Regex

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
                    val bitcoinTxs: List[BitcoinTransaction]) extends TCSBlock{


  /**
    * Returns a String representation of the block
    *
    * @return String representation
    */
  override def toString(): String = {
    val stringTransactions: String = "[ " + bitcoinTxs.map(tx => tx.toString() + " ") + "]"
    return hash + " " + date + " " + blockSize + " " + height + " " + stringTransactions
  }

  def getMiningPool(): String = {
    val firstTransaction: BitcoinTransaction = bitcoinTxs.head
    var pool: String = null

    if(firstTransaction.inputs.head.isCoinbase) {
      val programByte: Array[Byte] = firstTransaction.inputs.head.inScript.getProgram()
      if(programByte != null) {
        val hex: String = programByte.map("%02x".format(_)).mkString
        val program: String = new String(DatatypeConverter.parseHexBinary(hex))
        pool = getPoolByHexCode(program)
      }
    }

    return pool
  }

  private def getPoolByHexCode(hex: String): String = {
    var returned: String = null;

    // Known pool codes
    if(hex.indexOf("416e74506f6f6c3") > -1) returned = "AntMiner"
    if(hex.indexOf("736c757368") > -1) returned = "SlushPool"
    if(hex.indexOf("42544343") > -1) returned = "BTCCPool"
    if(hex.indexOf("4254432e434f4d") > -1) returned = "BTC.COM"
    if(hex.indexOf("566961425443") > -1) returned = "ViaBTC"
    if(hex.indexOf("4254432e544f502") > -1) returned = "BTC.TOP"
    if(hex.indexOf("426974436c7562204e6574776f726b") > -1) returned = "Bitclub Network"
    if(hex.indexOf("67626d696e657273") > -1) returned = "GBMiners"
    if(hex.indexOf("42697466757279") > -1) returned = "Bitfury"
    if(hex.indexOf("4269744d696e746572") > -1) returned = "BitMinter"

    // F2Pool does not have a unique identifier
    if(hex.indexOf("777868") > -1) returned = "F2Pool"
    if(hex.indexOf("66326261636b7570") > -1) returned = "F2Pool"
    if(hex.indexOf("68663235") > -1) returned = "F2Pool"
    if(hex.indexOf("73796a756e303031") > -1) returned = "F2Pool"
    if(hex.indexOf("716c7339") > -1) returned = "F2Pool"
    if(hex.indexOf("687578696e6767616f7a68616f") > -1) returned = "F2Pool"

    if(returned == null) {
      // If we have an unknown pool we try to read all ASCII char
      val pattern = new Regex("[a-zA-Z]+")
      returned = (pattern findAllIn  hex).mkString("|")
    }

    return returned
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