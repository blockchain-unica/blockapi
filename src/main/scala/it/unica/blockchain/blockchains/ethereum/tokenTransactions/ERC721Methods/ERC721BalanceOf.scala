package it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC721Methods

import java.util.Date

import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC721Transaction
import it.unica.blockchain.blockchains.ethereum.{EthereumAddress, EthereumContract}
import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

/** Defines the ERC721 method BalanceOf
  *
  * @param hash             transaction's hash
  * @param date             date in which the transaction has been published (extracted from the containing block)
  * @param nonce            transaction's nonce
  * @param blockHash        hash of the block containing this transaction
  * @param blockHeight      number of the block containing this transaction
  * @param transactionIndex index of the transaction inside its block
  * @param from             from
  * @param to               to
  * @param value            transaction's value
  * @param gasPrice         transaction's gas price
  * @param gas              transaction's gas
  * @param input            input of the transaction
  * @param addressCreated   Address of the created contract (if this transaction creates a contract)
  * @param publicKey        transaction's public key
  * @param raw              transaction's raw data
  * @param r                r part
  * @param s                s part
  * @param v                v part
  * @param method           the method called into the transaction
  * @param tokenOwner       the first parameter passed to the method
  */

class ERC721BalanceOf(
                       hash: String,
                       date: Date,

                       nonce: BigInt,
                       blockHash: String,
                       blockHeight: BigInt,
                       transactionIndex: BigInt,
                       from: EthereumAddress,
                       to: EthereumAddress,
                       value: BigInt,
                       gasPrice: BigInt,
                       gas: BigInt,
                       input: String,
                       addressCreated: EthereumAddress,
                       publicKey: String,
                       raw: String,
                       r: String,
                       s: String,
                       v: Int,

                       contract: EthereumContract,
                       requestOpt: Option[Request[_, EthGetTransactionReceipt]],

                       method: String,
                       val tokenOwner: EthereumAddress
                     ) extends ERC721Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt,method) {

}

object ERC721BalanceOf {

  /** This method takes the transaction's input and extract the element passed
    *
    * @param inputData   transaction's input
    * @return the method's name and the arguments passed
    */
  def getInputData(inputData: String): (String, EthereumAddress) = {
    val firstArg = 10

    val method: String = "balanceOf(address _owner)"

    val owner: String = inputData.substring(firstArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressOwner = refMethod.invoke(null, owner, classOf[Address]).asInstanceOf[Address]
    val ethAddressOwner = new EthereumAddress(addressOwner.toString)

    return (method, ethAddressOwner)
  }
}