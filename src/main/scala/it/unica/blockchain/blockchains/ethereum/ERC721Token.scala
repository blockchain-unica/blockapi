package it.unica.blockchain.blockchains.ethereum

import java.util.Date

class ERC721Token(
                   name: String,
                   address: EthereumAddress,
                   hashOriginatingTx: String,

                   isVerified: Boolean,
                   verificationDate: Date,

                   bytecode: String,
                   sourceCode: String
                 ) extends EthereumToken (name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode) {

}
