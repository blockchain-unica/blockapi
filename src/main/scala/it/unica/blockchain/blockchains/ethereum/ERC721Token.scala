package it.unica.blockchain.blockchains.ethereum

import java.util.Date

/** Defines functions available for ERC721 tokens
  *
  * @param name               contract's name
  * @param address            contract's address
  * @param hashOriginatingTx  transaction's hash that originated the contract
  * @param isVerified         contract's verification
  * @param verificationDate   contract's date verification
  * @param bytecode           contract's bytecode
  * @param sourceCode         contract's source code
  */

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
