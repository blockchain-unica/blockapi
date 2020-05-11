package it.unica.blockchain.blockchains.ethereum

import java.lang.reflect.Method
import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC20TransferFrom(
                         hash: String,
                         date: Date,

                         nonce: BigInt,
                         blockHash: String,
                         blockHeight: BigInt,
                         transactionIndex: BigInt,
                         from: String,
                         to: String,
                         value: BigInt,
                         gasPrice: BigInt,
                         gas: BigInt,
                         input: String,
                         addressCreated: String,
                         publicKey: String,
                         raw: String,
                         r: String,
                         s: String,
                         v: Int,

                         contract: EthereumContract,
                         requestOpt: Option[Request[_, EthGetTransactionReceipt]],

                         val method : String,
                         val tokenFrom :EthereumAddress,
                         val tokenTo :EthereumAddress,
                         val tokenValue :BigInt
                       ) extends ERC20Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {



}

object ERC20TransferFrom{

  def getInputData(inputData :String) :(String, EthereumAddress, EthereumAddress, BigInt) ={
    val method :String = "transferFrom(address from, address to, uint256 amount)"

    val from :String = inputData.substring(10,74)
    val to :String = inputData.substring(74,138)
    val value :String = inputData.substring(74)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Int], classOf[Class[_]])
    refMethod.setAccessible(true)

    val addressFrom = refMethod.invoke(null, from, 0, classOf[Address]).asInstanceOf[Address]
    val ethAddressFrom = new EthereumAddress(addressFrom.toString)

    val addressTo = refMethod.invoke(null, to, 0, classOf[Address]).asInstanceOf[Address]
    val ethAddressTo = new EthereumAddress(addressTo.toString)

    val amount = refMethod.invoke(null, value, 0, classOf[Uint256]).asInstanceOf[Uint256]

    return (method, ethAddressFrom, ethAddressTo, amount.getValue())
  }
}
