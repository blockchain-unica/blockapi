package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils

case class EthereumToken(
                          override val name: String,
                          override val address: String,
                          override val hashOriginatingTx: String,

                          override val isVerified: Boolean,
                          override val verificationDate: Date,

                          override val bytecode: String,
                          override val sourceCode: String
                        ) extends EthereumContract(name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode) {

  /**
    * This method checks if there is a token name in bytecode, if not returns "Unknown"
    *
    * We must find some instruction in bytecode that loads in memory a string. The candidate opcode is PUSH32,
    * 7f in Ethereum VM bytecode. The sequence between the first function call and return instructions is the name of the token
    *
    * The string "8152602001" is a sequence of instructions described by a couple of four number in hexadecimal:
    * the first one identifies opcode, the second one the value. This sequence preceded by 7f determines a string load
    * "81525081" are instruction of string load succeed
    *
    * @return token's name
    *
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def getTokenName(): String = {

    try {   //we don't know if someone wrote name in contract code

      val tokenName = new String(   //get the value of PUSH32 instruction in bytecode
        Hex.decodeHex(              //converts value from hex to string
          StringUtils.substringBetween(bytecode, "81526020017f", "81525081").toCharArray
        ), "UTF-8")

      if (tokenName == "address,bytes)"){ //sometimes catch something wrong like this load in memory "address,bytes)" when there is no name in bytecode
        "Unknown"
      } else {
        tokenName
      }

    } catch {
      case np : NullPointerException => "Unknown" //this happen when there's no name in bytecode
      case de : DecoderException => "Unknown" //this happen when found strange symbol like therefore sign (∴)
    }

  }

  /**
    * This method checks if there is a token symbol in bytecode, if not returns "Unknown"
    *
    * The approach is similar to getTokenName function: we must find some instruction in bytecode that loads in memory a string.
    * The candidate opcode is PUSH32, 7f in Ethereum VM bytecode. The sequence from the PUSH32's load name contains the symbol of the token.
    *
    * The string "8152602001" is a sequence of instructions described by a couple of four number in hexadecimal:
    * the first one identifies opcode, the second one the value. This sequence preceded by 7f determines a string load
    * "81525081" are instruction of string load succeed
    *
    * @return token's symbol
    *
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def getTokenSymbol(): String = {

    try { //we don't know if someone wrote symbol in contract code

      val temp = StringUtils.substringAfter(bytecode, "81526020017f")   //check every bytecode from token name load in memory, if exists

      val symbol = new String(  //then, take the next string value in PUSH32 instruction. It's always a token symbol
        Hex.decodeHex(
          StringUtils.substringBetween(temp, "81526020017f", "81525081").toCharArray
        ), "UTF-8")

      symbol

    } catch {
      case np : NullPointerException => "Unknown" //this happen when there's no name in bytecode
      case de : DecoderException => "Unknown" //this happen when found strange symbol like therefore sign (∴)
    }

  }

}
