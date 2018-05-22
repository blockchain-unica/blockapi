package tcs.blockchain.ethereum

import java.util.Date

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameterName, Request}
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import tcs.blockchain.Transaction
import tcs.externaldata.contracts.Etherscan.getVerifiedContractFromEtherscan


/**
  * Defines an Ethereum Transaction
  *
  * @param hash transaction's hash
  * @param date date in which the transaction has been published (extracted from the containing block)
  * @param nonce transaction's nonce
  * @param blockHash hash of the block containing this transaction
  * @param blockHeight number of the block containing this transaction
  * @param transactionIndex index of the transaction inside its block
  * @param from from
  * @param to to
  * @param value transaction's value
  * @param gasPrice transaction's gas price
  * @param gas transaction's gas
  * @param input input of the transaction
  * @param addressCreated Address of the created contract (if this transaction creates a contract)
  * @param publicKey transaction's public key
  * @param raw transaction's raw data
  * @param r r part
  * @param s s part
  * @param v v part
  */
case class EthereumTransaction(
                                override val hash: String,
                                override val date: Date,

                                val nonce: BigInt,
                                val blockHash: String,
                                val blockHeight: BigInt,
                                val transactionIndex: BigInt,
                                val from: String,
                                val to: String,
                                val value: BigInt,
                                val gasPrice: BigInt,
                                val gas: BigInt,
                                val input: String,
                                val addressCreated: String,
                                val publicKey: String,
                                val raw: String,
                                val r: String,
                                val s: String,
                                val v: Int,

                                val contract : EthereumContract,
                                val requestOpt: Option[Request[_, EthGetTransactionReceipt]]
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

  def hasContract : Boolean = {
    return contract != null
  }
}

/**
  * Factories for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
  */
object EthereumTransaction{

  private var web3j : Web3j = null

  /**
    * Factory for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
    * Returns an EthereumTransaction, given it's Web3J representation
    *
    * @param tx Web3J representation of this transaction
    * @return new EthereumTransaction
    */
  def factory(tx: TransactionObject, txDate: Date, receipt: Option[Request[_, EthGetTransactionReceipt]], retrieveVerifiedContracts: Boolean, web3j: Web3j): EthereumTransaction = {

    this.web3j = web3j

    // If the transaction creates a contract, initialize it.
    var contract : EthereumContract = null
    if (tx.getCreates() != null) {
      if(retrieveVerifiedContracts) {
        contract = getVerifiedContract(tx)
      } else {
        contract = getContract(tx)
      }
    }

    new EthereumTransaction(tx.getHash, txDate, tx.getNonce, tx.getBlockHash, tx.getBlockNumber, tx.getTransactionIndex,
                                   tx.getFrom, tx.getTo, tx.getValue, tx.getGasPrice, tx.getGas, tx.getInput,
                                   tx.getCreates, tx.getPublicKey, tx.getRaw, tx.getR, tx.getS, tx.getV,
                                   contract, receipt)
  }


  /** */
  def getContract(tx: TransactionObject): EthereumContract = {
    new EthereumContract("", tx.getCreates, tx.getHash, false, null, getContractBytecode(tx.getCreates), null)
  }


  /**
    * This method parses HTML pages from etherscan.io to find whether or not a contract has been verified.
    * If so, it finds its name on the platform and its date of verification, then creates an EthereumContract.
    *
    * @param tx Web3J representation of the transaction containing data about the contract
    * @return An Ethereum contract. In case of success, its fields will be populated with the aforementioned data.
    *         Otherwise it returns a null value.
    */
  private def getVerifiedContract(tx: TransactionObject): EthereumContract = {

    var contract : EthereumContract = null

    try {

      val (name, date, source, isVerified) = getVerifiedContractFromEtherscan(tx)
      return new EthereumContract(name, tx.getCreates, tx.getHash, isVerified, date, getContractBytecode(tx.getCreates), source)

    } catch {
      case ioe: java.io.IOException => {ioe.printStackTrace(); return contract}
      case ste: java.net.SocketTimeoutException => {ste.printStackTrace(); return contract}
      case e: Exception => {e.printStackTrace(); return contract}
    }
  }

  private def getContractBytecode(contractAddress : String) : String = {
    this.web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send().getCode
  }
}