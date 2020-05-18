package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ERC721OwnerOf(
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
                     val tokenId: BigInt
                   ) extends ERC20Transaction(hash, date, nonce, blockHash, blockHeight, transactionIndex, from, to, value, gasPrice, gas, input, addressCreated, publicKey, raw, r, s, v, contract, requestOpt) {

}

object ERC721OwnerOf {

  def getInputData(inputData: String): (String, BigInt) = {
    val argDim = 64
    val firstArg = 10

    val method: String = "ownerOf(uint256 _tokenId)"

    val tokenId: String = inputData.substring(firstArg)

    val refMethod = classOf[TypeDecoder].getDeclaredMethod("decode", classOf[String], classOf[Class[_]])
    refMethod.setAccessible(true)

    val id = refMethod.invoke(null, tokenId, classOf[Uint256]).asInstanceOf[Uint256]

    return (method, id.getValue)
  }
}

