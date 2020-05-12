package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt

class ETHTokenTransaction (
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
                          ) extends EthereumTransaction (hash,date,nonce,blockHash,blockHeight,transactionIndex,from,to,value,gasPrice,gas,input,addressCreated,publicKey,raw,r,s,v,contract,requestOpt) {

}
