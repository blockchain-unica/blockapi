package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import it.unica.blockchain.blockchains.ethereum.TokenType.TokenType
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameterName, Request}
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

import scala.io.Source

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

  def factory(web3j: Web3j, hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    this.web3j = web3j

    val index = input.indexOf("0x")
    var methodBytecode: String = "No Function"

    if (input != "0x" && input.length >= 10) //Is needed to be sure that there is at least the method call
      methodBytecode = input.substring(index + 2, index + 10)

    methodBytecode match {
      //case "18160ddd" => //totalSupply()

      case "dd62ed3e" => //allowance(address,address)
        allowance(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "095ea7b3" => // approve(address,uint256)
        approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "70a08231" =>  // balanceOf(address)
        balanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "081812fc" => // getApproved(uint256)
        getApproved(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "e985e9c5" => // isApprovedForAll(address,address)
        isApprovedForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "6352211e" =>  // ownerOf(uint256)
        ownerOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "42842e0e" => // safeTransferFrom(address,address,uint256)
        safeTransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "b88d4fde" => // safeTransferFrom(address,address,uint256,bytes)
        safeTransferFromWithBytes(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "a22cb465" => // setApprovalForAll(address,bool)
        setApprovalForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "a9059cbb" => //transfer(address,uint256)
        transfer(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case "23b872dd" => // transferFrom(address,address,uint256)
        transferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)

      case _ => //Not a Token function
        new EthereumTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  /**This function check out if the address is a token searching into the files. In case the address
    * is not found it will return a None type, indicating that the address is not into those files.
    * @return The type of the token.
    */
  private def ERCTxCheck (address :EthereumAddress): TokenType ={
    val file_path_ERC20 = "src/main/scala/it/unica/blockchain/externaldata/token/ERC20.txt"
    val file_path_ERC721 = "src/main/scala/it/unica/blockchain/externaldata/token/ERC721.txt"

    if(fileCheck(file_path_ERC20, address.address))
      TokenType.ERC20
    else if(fileCheck(file_path_ERC721, address.address))
      TokenType.ERC721
    else
      additionalControl(address)
  }

  /** This function checks if a string is stored into a file at the given path.
    * @param path a string representing a file source path.
    * @param address the string to search into the file.
    * @return true if the string has been found, false otherwise.
    */
  private def fileCheck(path :String, address : String): Boolean ={
    val bufferedSource = Source.fromFile(path)
    val list = bufferedSource.getLines.toList

    bufferedSource.close

    if(list.contains(address))
      true
    else
      false
  }

  /** This function is needed to perform an additional control to be sure that the given
    *  address is not a token
    * @return a token type, None if the address is not a token
    * @return an integer representing the divisibility for ERC20 tokens
    */
  private def additionalControl(address : EthereumAddress): TokenType ={
    val contract = contractType(address)
    contract match {
      case _: ERC20Token =>
        TokenType.ERC20
      case _: ERC721Token =>
        TokenType.ERC721
      case _ =>
        TokenType.None
    }
  }

  /**This function controls if the given address is a contract
    * @return An EthereumContract or null
    */
  private def contractType (to :EthereumAddress): EthereumContract ={

    val bytecode = getContractBytecode(to.address)
    if(bytecode != "0x"){
      return EthereumContract.factory("", to, "", false, null, bytecode, null, false)
    }
    null
  }

  private def getContractBytecode(contractAddress : String) : String = {
    this.web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send().getCode
  }

  private def allowance( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC20 =>
        val (method, tokenOwner, tokenSpender) = ERC20Allowance.getInputData(input)
        new ERC20Allowance(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner, tokenSpender)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def approve( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC20 =>
        val (method, tokenSpender, tokenValue) = ERC20Approve.getInputData(input)
        new ERC20Approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenSpender, tokenValue)
      case TokenType.ERC721 =>
        val (method, tokenApproved, tokenId) = ERC721Approve.getInputData(input)
        new ERC721Approve(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenApproved, tokenId)
      case TokenType.None =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def balanceOf( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC20 =>
        val (method, tokenOwner) = ERC20BalanceOf.getInputData(input)
        new ERC20BalanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner)
      case TokenType.ERC721 =>
        val (method, tokenOwner) = ERC721BalanceOf.getInputData(input)
        new ERC721BalanceOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner)
      case TokenType.None =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def getApproved( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC721 =>
        val (method, tokenId) = ERC721GetApproved.getInputData(input)
        new ERC721GetApproved(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenId)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def isApprovedForAll( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC721 =>
        val (method, tokenOwner, tokenOperator) = ERC721IsApprovedForAll.getInputData(input)
        new ERC721IsApprovedForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOwner, tokenOperator)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def ownerOf( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC721 =>
        val (method, tokenId) = ERC721OwnerOf.getInputData(input)
        new ERC721OwnerOf(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenId)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def safeTransferFrom( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC721 =>
        val (method, tokenFrom, tokenTo, tokenId) = ERC721SafeTransferFrom.getInputData(input)
        new ERC721SafeTransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenId)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def safeTransferFromWithBytes( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC721 =>
        val (method, tokenFrom, tokenTo, tokenId, tokenBytes) = ERC721SafeTransferFromWithBytes.getInputData(input)
        new ERC721SafeTransferFromWithBytes(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenId, tokenBytes)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def setApprovalForAll( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC721 =>
        val (method, tokenOperator, tokenApproved) = ERC721SetApprovalForAll.getInputData(input)
        new ERC721SetApprovalForAll(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenOperator, tokenApproved)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def transfer( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC20 =>
        val (method, tokenTo, tokenValue) = ERC20Transfer.getInputData(input)
        new ERC20Transfer(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenTo, tokenValue)
      case _ =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }

  private def transferFrom( hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction ={
    ERCTxCheck(to) match {
      case TokenType.ERC20 =>
        val (method, tokenFrom, tokenTo, tokenValue) = ERC20TransferFrom.getInputData(input)
        new ERC20TransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenValue)
      case TokenType.ERC721 =>
        val (method, tokenFrom, tokenTo, tokenId) = ERC721TransferFrom.getInputData(input)
        new ERC721TransferFrom(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt, method, tokenFrom, tokenTo, tokenId)
      case TokenType.None =>
        new ETHTokenTransaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)
    }
  }
}
 //(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt)