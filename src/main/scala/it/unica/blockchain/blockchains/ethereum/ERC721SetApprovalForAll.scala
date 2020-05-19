package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC721SetApprovalForAll (
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

                                val method : String,
                                val tokenOperator :EthereumAddress,
                                val tokenApproved :Boolean
                              ) extends ERC721Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {



}

object ERC721SetApprovalForAll{

  def getInputData(inputData :String) :(String, EthereumAddress, Boolean) ={
    val argDim = 64
    val firstArg = 10
    val secondArg = firstArg + argDim

    val method :String = "setApprovalForAll(address _operator, bool _approved)"

    val operator :String = inputData.substring(firstArg, secondArg)
    val approved :String = inputData.substring(secondArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressOperator = refMethod.invoke(null, operator, classOf[Address]).asInstanceOf[Address]
    val ethAddressOperator = new EthereumAddress(addressOperator.toString)

    val addressApproved = refMethod.invoke(null, approved, classOf[Boolean]).asInstanceOf[Boolean]

    return (method, ethAddressOperator, addressApproved)
  }
}

