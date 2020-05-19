package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC721Approve (
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
                      val tokenApproved :EthereumAddress,
                      val tokenId :Uint256
                    ) extends ERC721Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {



}

object ERC721Approve{

  def getInputData(inputData :String) :(String, EthereumAddress, Uint256) ={
    val argDim = 64
    val firstArg = 10
    val secondArg = firstArg + argDim

    val method :String = "approve(address _approved, uint256 _tokenId) "

    val approved :String = inputData.substring(firstArg, secondArg)
    val tokenId :String = inputData.substring(secondArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressApproved = refMethod.invoke(null, approved, classOf[Address]).asInstanceOf[Address]
    val ethAddressApproved = new EthereumAddress(addressApproved.toString)

    val id = refMethod.invoke(null, tokenId, classOf[Uint256]).asInstanceOf[Uint256]

    return (method, ethAddressApproved, id)
  }
}

