package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC721IsApprovedForAll (
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
                               val tokenOwner :EthereumAddress,
                               val tokenOperator :EthereumAddress
                             ) extends ERC721Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {



}

object ERC721IsApprovedForAll{

  def getInputData(inputData :String) :(String, EthereumAddress, EthereumAddress) ={
    val argDim = 64
    val firstArg = 10
    val secondArg = firstArg + argDim

    val method :String = "isApprovedForAll(address _owner, address _operator)"

    val owner :String = inputData.substring(firstArg, secondArg)
    val operator :String = inputData.substring(secondArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressOwner = refMethod.invoke(null, owner, classOf[Address]).asInstanceOf[Address]
    val ethAddressOwner = new EthereumAddress(addressOwner.toString)

    val addressOperator = refMethod.invoke(null, operator, classOf[Address]).asInstanceOf[Address]
    val ethAddressOperator = new EthereumAddress(addressOperator.toString)

    return (method, ethAddressOwner, ethAddressOperator)
  }
}

