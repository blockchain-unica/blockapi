package tcs.blockchain.ethereum

import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import tcs.blockchain.Transaction

/**
  * Defines an Ethereum Transaction
  *
  * @param hash transaction's hash
  * @param nonce transaction's nonce
  * @param blockHash hash of the block containing this transaction
  * @param blockNumber number of the block containing this transaction
  * @param transactionIndex index of the transaction inside its block
  * @param from from
  * @param to to
  * @param value transaction's value
  * @param gasPrice transaction's gas price
  * @param gas transaction's gas
  * @param input input of the transaction
  * @param creates true if this transaction creates a contract
  * @param publicKey transaction's public key
  * @param raw transaction's raw data
  * @param r r part
  * @param s s part
  * @param v v part
  */
case class EthereumTransaction(
                          hash: String,
                          nonce: BigInt,
                          blockHash: String,
                          blockNumber: BigInt,
                          transactionIndex: BigInt,
                          from: String,
                          to: String,
                          value: BigInt,
                          gasPrice: BigInt,
                          gas: BigInt,
                          input: String,
                          creates: String,
                          publicKey: String,
                          raw: String,
                          r: String,
                          s: String,
                          v: Int
                         ) extends Transaction

/**
  * Factories for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
  */
object EthereumTransaction{

  /**
    * Factory for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
    * Returns an EthereumTransaction, given it's Web3J representation
    *
    * @param tx Web3J representation of this transaction
    * @return new EthereumTransaction
    */
  def factory(tx: TransactionObject): EthereumTransaction = {
    new EthereumTransaction(tx.getHash, tx.getNonce, tx.getBlockHash, tx.getBlockNumber, tx.getTransactionIndex,
                                   tx.getFrom, tx.getTo, tx.getValue, tx.getGasPrice, tx.getGas, tx.getInput,
                                   tx.getCreates, tx.getPublicKey, tx.getRaw, tx.getR, tx.getS, tx.getV)
  }
}