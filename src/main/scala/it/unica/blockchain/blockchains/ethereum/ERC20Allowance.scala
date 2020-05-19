package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC20Allowance (
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

                       val method: String,
                       val tokenOwner: EthereumAddress,
                       val tokenSpender: EthereumAddress
                     ) extends ERC20Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {


}

object ERC20Allowance {

  def getInputData(inputData: String): (String, EthereumAddress, EthereumAddress) = {
    val argDim = 64
    val firstArg = 10
    val secondArg = firstArg + argDim

    val method: String = "allowance(address _owner, address _spender)"

    val owner: String = inputData.substring(firstArg, secondArg)
    val spender: String = inputData.substring(secondArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressOwner = refMethod.invoke(null, owner, classOf[Address]).asInstanceOf[Address]
    val ethAddressOwner = new EthereumAddress(addressOwner.toString)

    val addressSpender = refMethod.invoke(null, spender, classOf[Address]).asInstanceOf[Address]
    val ethAddressSpender = new EthereumAddress(addressSpender.toString)

    return (method, ethAddressOwner, ethAddressSpender)
  }
}
