package tcs.blockchain.ethereum

import java.util.Date

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils

import scala.util.matching.Regex


case class EthereumContract(
                             val name: String,
                             val address: String,
                             val hashOriginatingTx: String,

                             val isVerified: Boolean,
                             val verificationDate: Date,

                             val bytecode: String,
                             val sourceCode: String
                           ) {

  val usesPermissions = isVerified && checkPermissions

  /**
    * This method can be extended to implement smarter ways to find permissions,
    * right now it just does a basic string search.
    *
    * @return true if sourceCode contains permissions, false otherwise
    */
  private def checkPermissions(): Boolean = {

    return sourceCode.contains("modifier onlyOwner()")
  }

  /**
    * This method checks if the contract is ERC20 compliant analyzing the bytecode
    * Get a ERC20 standard token in Ethereum, it must implements the following functions:
    * - totalSupply() public constant returns (uint);
    * - balanceOf(address tokenOwner) public constant returns (uint balance);
    * - allowance(address tokenOwner, address spender) public constant returns (uint remaining);
    * - transfer(address to, uint tokens) public returns (bool success);
    * - approve(address spender, uint tokens) public returns (bool success);
    * - transferFrom(address from, address to, uint tokens) public returns (bool success);
    *
    * source: https://ethereum.stackexchange.com/questions/38381/how-can-i-identify-that-transaction-is-erc20-token-creation-contract?answertab=oldest#tab-top
    *
    * @return true if the contract is ERC20 compliant, false otherwise
    */

  def isERC20Compliant(): Boolean = {
    bytecode.contains("18160ddd") &&    //checks totalSupply() declaration
      bytecode.contains("70a08231") &&  //checks balanceOf(address) declaration
      bytecode.contains("dd62ed3e") &&  //checks allowance(address,address) declaration
      bytecode.contains("a9059cbb") &&  //checks transfer(address,uint256) declaration
      bytecode.contains("095ea7b3") &&  //checks approve(address,uint256) declaration
      bytecode.contains("23b872dd")     //checks transferFrom(address,address,uint256) declaration
  }

  /**
    * This method checks if there is a token name in bytecode, if not returns "Unknown"
    *
    * We must find some instruction in bytecode that loads in memory a string. The candidate opcode is PUSH32,
    * 7f in Ethereum VM bytecode. The sequence between the first function call and return instructions is the name of the token
    *
    * @return token's name
    *
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def getTokenName(): String = {

    try {   //we don't know if someone wrote name in code

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
    *
    * @return token's symbol
    *
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def getTokenSymbol(): String = {

    try { //we don't know if someone wrote symbol in code

      val temp = StringUtils.substringAfter(bytecode, "81526020017f")   //check every bytecode from token name load in memory, if exists

      val symbol = new String(  //then, take the next string value in PUSH32 instruction. It's always a token symbol
        Hex.decodeHex(
          StringUtils.substringBetween(temp, "81526020017f", "81525081").toCharArray
        ), "UTF-8")

      if (symbol == "address,bytes)"){  //prevents errors during parsing variables
        "Unknown"
      } else {
        symbol
      }

    } catch {
      case np : NullPointerException => "Unknown" //this happen when there's no name in bytecode
      case de : DecoderException => "Unknown" //this happen when found strange symbol like therefore sign (∴)
    }

  }


  /**
    * This method finds token divisibility in bytecode, if not returns "Unknown"
    *
    * The idea is simple: in bytecode some uint8 load in memory. The challenge was find the uint8 (or PUSH1 instruction) that describes
    * token divisibility. The occurrence that found it was described in val pattern
    *
    * @return token's divisibility
    *
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def getTokenDivisibility(): String = {

    val pattern = new Regex("565b60([0-9]|[a-f])([0-9]|[a-f])8156") //catch any value. The most used is 18, but uint8 load until 255

    val stringa = StringUtils.substringBetween((pattern findAllIn bytecode).mkString(",")
        , "565b60", "8156")   //get the value from store in VM

    if (stringa == null){   //no token divisibility found? It happens

      "Unknown"

    } else {

      val num = Integer.parseInt(stringa, 16)   //it converts from base 16 to base 10

      num.toString  //return value in string

    }
  }

}
