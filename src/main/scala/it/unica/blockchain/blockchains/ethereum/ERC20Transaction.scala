package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC20Transaction (
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

object  ERC20Transaction{

  def factory(hash: String, date: Date, nonce: BigInt, blockHash: String, blockHeight: BigInt, transactionIndex: BigInt, from: EthereumAddress, to: EthereumAddress, value: BigInt, gasPrice: BigInt, gas: BigInt, input: String, addressCreated: EthereumAddress, publicKey: String, raw: String, r: String, s: String, v: Int, contract : EthereumContract, requestOpt: Option[Request[_, EthGetTransactionReceipt]]): ERC20Transaction ={
    val index = input.indexOf("0x")
    val methodBytecode = input.substring(index+2, index+10)

    methodBytecode match {
      //case "18160ddd" => //totalSupply
      //case "70a08231" => //balanceOf
      //case "dd62ed3e" => //allowance
      //case "a9059cbb" => //transfer
      //case "095ea7b3" => //approve
      case "23b872dd" => //transferFrom
        val (method, tokenFrom, tokenTo, tokenValue) = ERC20TransferFrom.getInputData(input)
        new ERC20TransferFrom (hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt, method, tokenFrom, tokenTo, tokenValue)
      case _ => //NonERC20Function
        new ERC20Transaction(hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt)
    }
  }
}
