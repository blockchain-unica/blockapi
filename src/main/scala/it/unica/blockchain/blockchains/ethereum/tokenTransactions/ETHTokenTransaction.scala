package it.unica.blockchain.blockchains.ethereum.tokenTransactions

import java.util.Date

import it.unica.blockchain.blockchains.ethereum.tokenUtils.TokenType.TokenType
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC20Methods._
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC721Methods._
import it.unica.blockchain.blockchains.ethereum.tokens.{ERC20Token, ERC721Token}
import it.unica.blockchain.blockchains.ethereum._
import it.unica.blockchain.blockchains.ethereum.tokenUtils.{TokenList, TokenType}
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.protocol.core.{DefaultBlockParameterName, Request}

/** This class is used to check if a method has been called into the transaction and
  * find out which was.
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

class ETHTokenTransaction (
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

                            contract : EthereumContract,
                            requestOpt: Option[Request[_, EthGetTransactionReceipt]]
                          ) extends EthereumTransaction (hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt) {

}

object  ETHTokenTransaction {
  private var web3j : Web3j = null

  /** This function controls if a token method has been called into the thransaction's input and if
    * the token is a standard ERC
    */
  def factory(web3j: Web3j, hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    this.web3j = web3j

    var erc20Method: ERC20Transaction = null
    var erc721Method: ERC721Transaction = null

    if(checkInput(input)) {
      erc20Method = ERC20Transaction.findERC20Method(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]])
      erc721Method = ERC721Transaction.findERC721Method(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract: EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]])
    }
    var txType = TokenType.None

    if(erc20Method != null || erc721Method != null) // To improve performance we check before if a method has been called
      txType = ERCTxCheck(to)

    if(erc20Method != null && txType == TokenType.ERC20)
      erc20Method
    else if(erc721Method != null && txType == TokenType.ERC721)
      erc721Method
    else
      new EthereumTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
  }

  /**This function checks out if the address is a token, searching into the map. In case the address
    * is not found it will return a None type. None indicates that the address is not into the map.
    * @return The type of the token.
    */
  private def ERCTxCheck (address :EthereumAddress): TokenType ={
    var tipo = TokenType.None
    val listERCAddress = TokenList.getList()

    if(listERCAddress.contains(address.address)) {
      tipo = listERCAddress(address.address)
    } else {
      tipo = additionalControl(address)
    }

    return tipo
  }

  /** This function is needed to perform an additional control to be sure that the given
    *  address is not a token
    * @param address an EthereumAddress
    * @return a token type, None if the address is not a token
    */
  private def additionalControl(address : EthereumAddress): TokenType ={
    val contract = contractType(address)
    contract match {
      case _: ERC20Token =>
        TokenList.add(address.address, TokenType.ERC20)
        TokenType.ERC20
      case _: ERC721Token =>
        TokenList.add(address.address, TokenType.ERC721)
        TokenType.ERC721
      case _ =>
        TokenType.None
    }
  }

  /**This function controls if the given address is a contract
    * @param to an EthereumAddress
    * @return An EthereumContract or null
    */
  private def contractType (to :EthereumAddress): EthereumContract ={

    val bytecode = getContractBytecode(to.address)
    if(bytecode != null && bytecode.length > 2){ // If the contract contains at least a method name after "0x"
      return EthereumContract.factory("", to, "", false, null, bytecode, null, false)
    }
    null
  }

  private def getContractBytecode(contractAddress : String) : String = {
    this.web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send().getCode
  }


  /** This function returns true if the input has the right length
    *
    * @param input the input
    */
  def checkInput(input: String): Boolean ={
    val input_lenght = 64
    val method_lenght = 10
    if(input != null &&
      (input.length() % input_lenght) - method_lenght == 0 && // to know if each input field has the right length
      input.matches("[a-zA-Z0-9]*"))
      true
    else
      false
  }


  /** This function returns true if the input has the right number of arguments required
    *
    * @param input The input
    * @param numOfArgs The number of args required
    */
  def checkInputArgs(input: String, numOfArgs: Int): Boolean ={
    val input_lenght = 64
    val method_lenght = 10
    if((input.length() - method_lenght) / input_lenght == numOfArgs)
      true
    else
      false
  }
}