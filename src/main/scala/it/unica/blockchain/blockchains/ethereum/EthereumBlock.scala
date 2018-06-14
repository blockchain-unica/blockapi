package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlock.{Block, TransactionObject}
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import it.unica.blockchain.blockchains.{Block => TCSBLock}
import it.unica.blockchain.utils.converter.DateConverter.getDateFromTimestamp
import it.unica.blockchain.externaldata.miningpools.MiningPools

import scala.collection.JavaConverters._

/**
  * Defines a block of the Etherum blockchain
  *
  * @param hash block hash
  * @param height block number
  * @param date timestamp of block
  * @param size size of block
  * @param txs list of block's transactions
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
  * @param gasLimit gasLimit
  * @param gasUsed gasUsed
  * @param internalTransactions list of block's internal transactions
  * @param uncles list of uncles' hashes
  * @param sealFields sealFields
  */
case class EthereumBlock(
                          override val hash: String,
                          override val height: BigInt,
                          override val date: Date,
                          override val size: BigInt,
                          override val txs: List[EthereumTransaction],

                          val parentHash: String,
                          val nonce: BigInt,
                          val sha3Uncles: String,
                          val logsBloom: String,
                          val transactionRoot: String,
                          val stateRoot: String,
                          val receiptsRoot: String,
                          val author: String,
                          val miner: String,
                          val mixHash: String,
                          val difficulty: BigInt,
                          val totalDifficulty: BigInt,
                          val extraData: String,
                          val gasLimit: BigInt,
                          val gasUsed: BigInt,
                          val internalTransactions: List[EthereumInternalTransaction],
                          val uncles: List[String],
                          val sealFields: List[String]
                   ) extends TCSBLock{
  /**
    * toString override
    * @return string representation of the Ethereum block
    */
  override def toString: String = {
    "number: " + this.height + "; hash: " + this.hash
  }

  /**
    * Get a string representation of the mining pool using the extradata infos
    * @return a string corresponding to the mining pool that created this block
    */
  override def getMiningPool: String = {
    MiningPools.getEthereumPool(extraData)
  }
}

/**
  * Factories for [[it.unica.blockchain.blockchains.ethereum.EthereumBlock]] instances
  */
object EthereumBlock{

  /**
    * Factory for [[it.unica.blockchain.blockchains.ethereum.EthereumBlock]] instances.
    * Returns an EthereumBlock, given it's Web3J representation and
    * the list of its internal transactions
    *
    * @param block Web3J representation of this block
    * @param internalTransactions block's internal transactions
    * @return new EtherumBlock
    */
  def factory(block: Block, internalTransactions: List[EthereumInternalTransaction], transactionReceipts: Map[String, Request[_, EthGetTransactionReceipt]], retrieveVerifiedContracts: Boolean, web3j: Web3j): EthereumBlock = {

    val transactions: List[EthereumTransaction] =
      block.getTransactions.asScala.toList
        .map(_.asInstanceOf[TransactionObject])
        .map((tx) => EthereumTransaction.factory(tx, getDateFromTimestamp(block.getTimestamp), transactionReceipts.get(tx.get().getHash), retrieveVerifiedContracts, web3j))
    var sealFields = block.getSealFields
    if(sealFields == null){
      sealFields = List[String]().asJava
    }

    new EthereumBlock( block.getHash, block.getNumber, getDateFromTimestamp(block.getTimestamp),
                      block.getSize, transactions, block.getParentHash, block.getNonce, block.getSha3Uncles,
                      block.getLogsBloom, block.getTransactionsRoot, block.getStateRoot, block.getReceiptsRoot,
                      block.getAuthor, block.getMiner, block.getMixHash, block.getDifficulty, block.getTotalDifficulty,
                      block.getExtraData, block.getGasLimit, block.getGasUsed,
                      internalTransactions, block.getUncles.asScala.toList, sealFields.asScala.toList)
  }

}