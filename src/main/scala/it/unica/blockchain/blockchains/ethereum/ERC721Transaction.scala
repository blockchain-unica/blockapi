package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC721Transaction (
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
                        ) extends ETHTokenTransaction (hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt) {


}

object  ERC721Transaction{

  def factory(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC721Transaction ={
    val index = input.indexOf("0x")
    var methodBytecode : String = "No Function"

    if(input != "0x")
      methodBytecode = input.substring(index+2, index+10)

    methodBytecode match {
      //case "70a08231" => // balanceOf(address)
      //case "6352211e" => // ownerOf(uint256)
      //case "095ea7b3" => // approve(address,uint256)
      //case "081812fc" => // getApproved(uint256)
      //case "a22cb465" => // setApprovalForAll(address,bool)
      //case "e985e9c5" => // isApprovedForAll(address,address)
      case "23b872dd" =>   // transferFrom(address,address,uint256)
        val (method, tokenFrom, tokenTo, tokenValue) = ERC721TransferFrom.getInputData(input)
        new ERC721TransferFrom (hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt, method, tokenFrom, tokenTo, tokenValue)
      //case "42842e0e" => // safeTransferFrom(address,address,uint256)
      //case "b88d4fde" => // safeTransferFrom(address,address,uint256,bytes)
      //case "150b7a02" => // onERC721Received(address,address,uint256,bytes)
      case _ => //Not a ERC721 function
        new ERC721Transaction(hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt)
    }
  }
}
