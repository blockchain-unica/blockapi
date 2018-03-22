package tcs.blockchain.ethereum

import java.util.concurrent.CompletableFuture

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import org.web3j.protocol.core.methods.response.{EthGetTransactionReceipt, TransactionReceipt}
import org.web3j.protocol.http.HttpService
import shapeless.ops.nat.GT.>
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
                          v: Int,
                          verifiedContract: String,
                          contractName: String,
                          verificationDay: String,
                          requestOpt: Option[Request[_, EthGetTransactionReceipt]]
                         ) extends Transaction {

  def getContractAddress(): Option[String] = {
    if (requestOpt.isDefined) {
      val request = requestOpt.get
      val response = request.send()
      val receiptOpt = response.getTransactionReceipt
      if (receiptOpt.isPresent && receiptOpt.get.getContractAddress != null) {
        return Some(receiptOpt.get.getContractAddress)
      }
    }
    None
  }
}

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
  def factory(tx: TransactionObject, receipt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction = {


    val (verifiedContract, contractName, verificationDay) = getVerifiedContracts

    new EthereumTransaction(tx.getHash, tx.getNonce, tx.getBlockHash, tx.getBlockNumber, tx.getTransactionIndex,
                                   tx.getFrom, tx.getTo, tx.getValue, tx.getGasPrice, tx.getGas, tx.getInput,
                                   tx.getCreates, tx.getPublicKey, tx.getRaw, tx.getR, tx.getS, tx.getV,
                                   "placeholder_verified", "placeholder_contractname", "placeholder_verificationday",
                                   receipt)
  }

  private def getVerifiedContracts = {



  }

}