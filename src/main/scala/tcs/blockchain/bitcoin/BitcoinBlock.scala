package tcs.blockchain.bitcoin

import java.util.Date

import org.bitcoinj.core.{Block, Sha256Hash}
import tcs.blockchain.{Block => TCSBlock}

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
    var pool: String = "Unknown"

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

  private def getPoolByHexCode(hex: String): String = {

    // Known pool codes listed on blockchain.info
    // See https://github.com/blockchain/Blockchain-Known-Pools/blob/master/pools.json for more
    if(hex.contains("416e74506f6f6c3")) return "AntPool"
    if(hex.contains("42697446757279")) return "BitFury"
    if(hex.contains("736c757368")) return "SlushPool"
    if(hex.contains("42544343")) return "BTCCPool"
    if(hex.contains("4254432e434f4d")) return "BTC.COM"
    if(hex.contains("566961425443")) return "ViaBTC"
    if(hex.contains("4254432e544f502")) return "BTC.TOP"
    if(hex.contains("426974436c7562204e6574776f726b")) return "Bitclub Network"
    if(hex.contains("67626d696e657273")) return "GBMiners"
    if(hex.contains("42697466757279")) return "Bitfury"
    if(hex.contains("4269744d696e746572")) return "BitMinter"
    if(hex.contains("4b616e6f")) return "KanoPool"
    if(hex.contains("426974636f696e2d5275737369612e7275")) return "BitcoinRussia"
    if(hex.contains("426974636f696e2d496e646961")) return "BitcoinIndia"
    if(hex.contains("425720506f6f6c")) return "BW.COM"
    if(hex.contains("3538636f696e2e636f6d")) return "58coin"
    if(hex.contains("706f6f6c2e626974636f696e2e636f6d")) return "Bitcoin.com"
    if(hex.contains("436f6e6e656374425443202d20486f6d6520666f72204d696e657273")) return "ConnectedBTC"

    // F2Pool does not have a unique identifier
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
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx, block.getTime)).toList

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
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx, block.getTime, UTXOmap, height)).toList

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
    val transactions: List[BitcoinTransaction] = block.getTransactions.asScala.map(tx => BitcoinTransaction.factory(tx, block.getTime)).toList

    return new BitcoinBlock(block.getHash, block.getTime, block.getMessageSize, 0, transactions)
  }
}