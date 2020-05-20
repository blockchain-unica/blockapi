package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

/** Defines a transaction that called an ERC20 function
  *
  * @param hash             transaction's hash
  * @param date             date in which the transaction has been published (extracted from the containing block)
  * @param nonce            transaction's nonce
  * @param blockHash        hash of the block containing this transaction
  * @param blockHeight      number of the block containing this transaction
  * @param transactionIndex index of the transaction inside its block
  * @param from             from
  * @param to               to
  * @param value            transaction's value
  * @param gasPrice         transaction's gas price
  * @param gas              transaction's gas
  * @param input            input of the transaction
  * @param addressCreated   Address of the created contract (if this transaction creates a contract)
  * @param publicKey        transaction's public key
  * @param raw              transaction's raw data
  * @param r                r part
  * @param s                s part
  * @param v                v part
  */

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

