package it.unica.blockchain.blockchains.ethereum.tokenTransactions

import java.util.Date

import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC721Methods.{ERC721Approve, ERC721BalanceOf, ERC721GetApproved, ERC721IsApprovedForAll, ERC721OwnerOf, ERC721SafeTransferFrom, ERC721SafeTransferFromWithBytes, ERC721SetApprovalForAll, ERC721TransferFrom}
import it.unica.blockchain.blockchains.ethereum.{EthereumAddress, EthereumContract}
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

/** Defines a transaction that called an ERC721 function
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
  */

class ERC721Transaction(
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

                         val method : String
                       ) extends ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {


}

object ERC721Transaction {

  /** This function matches the transaction's input with the pattern of known methods and then creates an object
    */
  def findERC721Method(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val index = input.indexOf("0x")
    var methodBytecode: String = "No Function"

    if (input != "0x" && input.length >= 10) //Is needed to be sure that there is at least the method call
    methodBytecode = input.substring(index + 2, index + 10)

    //If the input contains a method call then is controlled which type of contract has been called
    methodBytecode match {
      case "095ea7b3" => // approve(address,uint256)
        approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "70a08231" => // balanceOf(address)
        balanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "081812fc" => // getApproved(uint256)
        getApproved(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "e985e9c5" => // isApprovedForAll(address,address)
        isApprovedForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "6352211e" => // ownerOf(uint256)
        ownerOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "42842e0e" => // safeTransferFrom(address,address,uint256)
        safeTransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "b88d4fde" => // safeTransferFrom(address,address,uint256,bytes)
        safeTransferFromWithBytes(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "a22cb465" => // setApprovalForAll(address,bool)
        setApprovalForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "23b872dd" => // transferFrom(address,address,uint256)
        transferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case _ => //Not a Token function
        null
    }
  }

  /** Each function controls which type of contract has been called
    */

  private def approve(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenApproved, tokenId) = ERC721Approve.getInputData(input)
    new ERC721Approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenApproved, tokenId)
  }

  private def balanceOf(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenOwner) = ERC721BalanceOf.getInputData(input)
    new ERC721BalanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner)
  }

  private def getApproved(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenId) = ERC721GetApproved.getInputData(input)
    new ERC721GetApproved(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenId)
  }

  private def isApprovedForAll(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenOwner, tokenOperator) = ERC721IsApprovedForAll.getInputData(input)
    new ERC721IsApprovedForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner, tokenOperator)
  }

  private def ownerOf(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenId) = ERC721OwnerOf.getInputData(input)
    new ERC721OwnerOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenId)
  }

  private def safeTransferFrom(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenFrom, tokenTo, tokenId) = ERC721SafeTransferFrom.getInputData(input)
    new ERC721SafeTransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenId)
  }

  private def safeTransferFromWithBytes(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenFrom, tokenTo, tokenId, tokenBytes) = ERC721SafeTransferFromWithBytes.getInputData(input)
    new ERC721SafeTransferFromWithBytes(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenId, tokenBytes)
  }

  private def setApprovalForAll(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenOperator, tokenApproved) = ERC721SetApprovalForAll.getInputData(input)
    new ERC721SetApprovalForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOperator, tokenApproved)
  }

  private def transferFrom(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction = {
    val (method, tokenFrom, tokenTo, tokenId) = ERC721TransferFrom.getInputData(input)
    new ERC721TransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenId)
  }
}