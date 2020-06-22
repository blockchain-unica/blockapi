package it.unica.blockchain.blockchains.ethereum.tokenTransactions

import java.util.Date

import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC20Methods.{ERC20Allowance, ERC20Approve, ERC20BalanceOf, ERC20Transfer, ERC20TransferFrom}
import it.unica.blockchain.blockchains.ethereum.{EthereumAddress, EthereumContract}
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ETHTokenTransaction.checkInputArgs
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

/** Defines a transaction that called an ERC20 function
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

class ERC20Transaction(
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

object ERC20Transaction {

  /** This function matches the transaction's input with the pattern of known methods and then creates an object
    */
  def findERC20Method (hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction = {
    val index = input.indexOf("0x")
    var methodBytecode: String = "No Function"

    if (input != "0x" && input.length >= 10) //Is needed to be sure that there is at least the method call
    methodBytecode = input.substring(index + 2, index + 10)

    //If the input contains a method call then is controlled which type of contract has been called
    methodBytecode match {
      //case "18160ddd" => //totalSupply()

      case "dd62ed3e" => //allowance(address,address)
        allowance(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "095ea7b3" => // approve(address,uint256)
        approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "70a08231" => // balanceOf(address)
        balanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "a9059cbb" => //transfer(address,uint256)
        transfer(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "23b872dd" => // transferFrom(address,address,uint256)
        transferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case _ => //Not a Token function
        null
    }
  }

  /** Each function controls which type of contract has been called
    */

  private def allowance(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction = {
    if (checkInputArgs(input, 2)) {
      val (method, tokenOwner, tokenSpender) = ERC20Allowance.getInputData(input)
      new ERC20Allowance(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner, tokenSpender)
    }
    else
      null
  }

  private def approve(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction = {
    if (checkInputArgs(input, 2)) {
      val (method, tokenSpender, tokenValue) = ERC20Approve.getInputData(input)
      new ERC20Approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenSpender, tokenValue)
    }
    else
      null
  }

  private def balanceOf(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction = {
    if (checkInputArgs(input, 1)) {
      val (method, tokenOwner) = ERC20BalanceOf.getInputData(input)
      new ERC20BalanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner)
    }
    else
      null
  }

  private def transfer(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction = {
    if (checkInputArgs(input, 2)) {
      val (method, tokenTo, tokenValue) = ERC20Transfer.getInputData(input)
      new ERC20Transfer(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenTo, tokenValue)
    }
    else
      null
  }

  private def transferFrom(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction = {
    if (checkInputArgs(input, 3)) {
      val (method, tokenFrom, tokenTo, tokenValue) = ERC20TransferFrom.getInputData(input)
      new ERC20TransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenValue)
    }
    else
      null
  }
}

