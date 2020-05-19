package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC20Approve (
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
                     val tokenSpender: EthereumAddress,
                     val tokenValue: Uint256
                   ) extends ERC20Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {


}

object ERC20Approve {

  def getInputData(inputData: String): (String, EthereumAddress, Uint256) = {
    val argDim = 64
    val firstArg = 10
    val secondArg = firstArg + argDim

    val method: String = "approve(address _spender, uint256 _value)"

    val spender: String = inputData.substring(firstArg, secondArg)
    val value: String = inputData.substring(secondArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressSpender = refMethod.invoke(null, spender, classOf[Address]).asInstanceOf[Address]
    val ethAddressSpender = new EthereumAddress(addressSpender.toString)

    val amount = refMethod.invoke(null, value, classOf[Uint256]).asInstanceOf[Uint256]

    return (method, ethAddressSpender, amount)
  }
}

