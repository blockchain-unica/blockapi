package tcs.blockchain.ethereum

import org.web3j.protocol.core.methods.response.EthBlock.{Block, TransactionObject}

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
                          timeStamp: BigInt,
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
  def factory(block: Block, internalTransactions: List[EthereumInternalTransaction]): EthereumBlock = {
    val transactions: List[EthereumTransaction] =
      block.getTransactions.asScala.toList.map((tx) => EthereumTransaction.factory(tx.asInstanceOf[TransactionObject]))
    var sealFields = block.getSealFields
    if(sealFields == null){
      sealFields = List().asJava
    }
    new EthereumBlock(block.getNumber, block.getHash, block.getParentHash, block.getNonce, block.getSha3Uncles,
                      block.getLogsBloom, block.getTransactionsRoot, block.getStateRoot, block.getReceiptsRoot,
                      block.getAuthor, block.getMiner, block.getMixHash, block.getDifficulty, block.getTotalDifficulty,
                      block.getExtraData, block.getSize, block.getGasLimit, block.getGasUsed, block.getTimestamp,
                      transactions, internalTransactions, block.getUncles.asScala.toList, sealFields.asScala.toList)
  }
}