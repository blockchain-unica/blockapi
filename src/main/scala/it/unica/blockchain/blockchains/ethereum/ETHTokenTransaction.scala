package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ETHTokenTransaction (
                            override val hash: String,
                            override val date: Date,

                            override val nonce: BigInt,
                            override val blockHash: String,
                            override val blockHeight: BigInt,
                            override val transactionIndex: BigInt,
                            override val from: String,
                            override val to: String,
                            override val value: BigInt,
                            override val gasPrice: BigInt,
                            override val gas: BigInt,
                            override val input: String,
                            override val addressCreated: String,
                            override val publicKey: String,
                            override val raw: String,
                            override val r: String,
                            override val s: String,
                            override val v: Int,

                            override val contract : EthereumContract,
                            override val requestOpt: Option[Request[_, EthGetTransactionReceipt]]
                          ) extends EthereumTransaction (hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt) {

}
