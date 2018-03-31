package tcs.blockchain.ethereum

import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlock.{Block, TransactionObject}
import org.web3j.protocol.core.methods.response.{EthGetTransactionReceipt, TransactionReceipt}
import tcs.blockchain.{Block => TCSBLock}

import scala.collection.JavaConverters._

/**
  * Defines a block of the Etherum blockchain
  *
  * @param number block number
  * @param hash block hash
  * @param parentHash parent block hash
  * @param nonce nonce
  * @param sha3Uncles sha3 representation of block uncles
  * @param logsBloom logsBloom
  * @param transactionRoot transactionRoot
  * @param stateRoot stateRoot
  * @param receiptsRoot receiptsRoot
  * @param author author of block
  * @param miner miner of block
  * @param mixHash mixHash
  * @param difficulty difficulty
  * @param totalDifficulty totalDifficulty
  * @param extraData block extraData
  * @param size size of block
  * @param gasLimit gasLimit
  * @param gasUsed gasUsed
  * @param timeStamp timestamp of block
  * @param transactions list of block's transactions
  * @param internalTransactions list of block's internal transactions
  * @param uncles list of uncles' hashes
  * @param sealFields sealFields
  */
case class EthereumBlock(
                          number: BigInt,
                          hash: String,
                          parentHash: String,
                          nonce: BigInt,
                          sha3Uncles: String,
                          logsBloom: String,
                          transactionRoot: String,
                          stateRoot: String,
                          receiptsRoot: String,
                          author: String,
                          miner: String,
                          mixHash: String,
                          difficulty: BigInt,
                          totalDifficulty: BigInt,
                          extraData: String,
                          size: BigInt,
                          gasLimit: BigInt,
                          gasUsed: BigInt,
                          timeStamp: Date,
                          transactions: List[EthereumTransaction],
                          internalTransactions: List[EthereumInternalTransaction],
                          uncles: List[String],
                          sealFields: List[String]
                   ) extends TCSBLock{
  /**
    * toString override
    * @return string representation of the Ethereum block
    */
  override def toString: String = {
    "number: " + this.number + "; hash: " + this.hash
  }

  /**
    * Get a string representation of the mining pool using the extradata infos
    * @return a string corresponding to the mining pool that created this block
    */
  def getMiningPool: String = {
    if(extraData.contains("7777772e62772e636f6d")) return "BW.COM"
    if(extraData.contains("65746865726d696e652d")) return "Ethermine"
    if(extraData.contains("e4b883e5bda9e7a59ee4bb99e9b1bc")) return "f2pool_2"
    if(extraData.contains("4554482e45544846414e532e4f52472d")) return "Sparkpool"
    if(extraData.contains("6e616e6f706f6f6c2e6f7267")) return "Nanopool"
    if(extraData.contains("4477617266506f6f6c")) return "DwarfPool"
    if(extraData.contains("7869786978697869")) return "bitclubpool"
    if(extraData.contains("657468706f6f6c2d")) return "Ethpool_2"
    if(extraData.contains("70616e64615f706f6f6c")) return "PandaPool"
    if(extraData.contains("786e706f6f6c2e636e")) return "xnpool.cn"
    if(extraData.contains("7575706f6f6c2e636e")) return "uupool.cn"
    if(extraData.contains("7761746572686f6c652e696f")) return "waterhole"
    if(extraData.contains("5553492054656368")) return "USI Tech"
    if(extraData.contains("7777772e627463632e72656e")) return "www.btcc.ren"
    if(extraData.contains("457468657265756d50504c4e532f326d696e657273")) return "EthereumPPLNS"
    if(extraData.contains("4f70656e457468657265756d506f6f6c2f74776574682e7477")) return "TwethPool"
    if(extraData.contains("d5830107068650617269747986312e32302e30826c69")) return "Coinotron_2"

    "Unknown"
  }
}

/**
  * Factories for [[tcs.blockchain.ethereum.EthereumBlock]] instances
  */
object EthereumBlock{

  /**
    * Factory for [[tcs.blockchain.ethereum.EthereumBlock]] instances.
    * Returns an EthereumBlock, given it's Web3J representation and
    * the list of its internal transactions
    *
    * @param block Web3J representation of this block
    * @param internalTransactions block's internal transactions
    * @return new EtherumBlock
    */
  def factory(block: Block, internalTransactions: List[EthereumInternalTransaction], transactionReceipts: Map[String, Request[_, EthGetTransactionReceipt]], retrieveVerifiedContracts: Boolean): EthereumBlock = {
    val transactions: List[EthereumTransaction] =
      block.getTransactions.asScala.toList
        .map(_.asInstanceOf[TransactionObject])
        .map((tx) => EthereumTransaction.factory(tx, block.getTimestamp, transactionReceipts.get(tx.get().getHash), retrieveVerifiedContracts))
    var sealFields = block.getSealFields
    if(sealFields == null){
      sealFields = List[String]().asJava
    }
    new EthereumBlock(block.getNumber, block.getHash, block.getParentHash, block.getNonce, block.getSha3Uncles,
                      block.getLogsBloom, block.getTransactionsRoot, block.getStateRoot, block.getReceiptsRoot,
                      block.getAuthor, block.getMiner, block.getMixHash, block.getDifficulty, block.getTotalDifficulty,
                      block.getExtraData, block.getSize, block.getGasLimit, block.getGasUsed,
                      new Date(block.getTimestamp.longValue()),
                      transactions, internalTransactions, block.getUncles.asScala.toList, sealFields.asScala.toList)
  }
}