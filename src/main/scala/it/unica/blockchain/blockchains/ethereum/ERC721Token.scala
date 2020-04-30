package it.unica.blockchain.blockchains.ethereum

import java.util.Date

class ERC721Token(
                   override val name: String,
                   override val address: String,
                   override val hashOriginatingTx: String,

                   override val isVerified: Boolean,
                   override val verificationDate: Date,

                   override val bytecode: String,
                   override val sourceCode: String
                 ) extends EthereumToken (name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode) {

}
