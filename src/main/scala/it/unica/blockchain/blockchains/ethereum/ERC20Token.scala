package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.apache.commons.lang3.StringUtils

import scala.util.matching.Regex

/** Defines functions available for ERC20 tokens
  *
  * @param name               contract's name
  * @param address            contract's address
  * @param hashOriginatingTx  transaction's hash that originated the contract
  * @param isVerified         contract's verification
  * @param verificationDate   contract's date verification
  * @param bytecode           contract's bytecode
  * @param sourceCode         contract's source code
  */

class ERC20Token(
                  name: String,
                  address: EthereumAddress,
                  hashOriginatingTx: String,

                  isVerified: Boolean,
                  verificationDate: Date,

                  bytecode: String,
                  sourceCode: String
                ) extends EthereumToken (name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode) {

  /**
    * This method finds token divisibility in bytecode, if not returns "Unknown"
    *
    * The idea is simple: in bytecode there are some uint8 load in memory. The challenge was find the uint8 (or PUSH1 instruction) that describes
    * token divisibility. The occurrence that found it was described in val pattern
    *
    * 60 is the hexadecimal opcode for PUSH1; the previous sequence identifies a token divisibility load in memory
    * Only token divisibility load in memory value is succeed by instruction "8156"
    *
    * @return token's divisibility
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def getTokenDivisibility(): String = {

    val pattern = new Regex("565b60([0-9]|[a-f])([0-9]|[a-f])8156") //catch any value until 255. The most used is 18

    val stringa = StringUtils.substringBetween((pattern findAllIn bytecode).mkString(",")
      , "565b60", "8156") //get the value from store in VM

    if (stringa == null) { //no token divisibility found?

      "Unknown"

    } else {

      val num = Integer.parseInt(stringa, 16) //it converts from base 16 to base 10

      num.toString //return value in string

    }
  }
}
