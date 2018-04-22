package tcs.blockchain.ethereum

import java.util.Date

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils


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
    * source: https://ethereum.stackexchange.com/questions/38381/how-can-i-identify-that-transaction-is-erc20-token-creation-contract?answertab=oldest#tab-top
    *
    * @return true if the contract is ERC20 compliant, false otherwise
    */

  def isERC20Compliant(): Boolean = {
    return bytecode.contains("18160ddd") &&
      bytecode.contains("70a08231") &&
      bytecode.contains("dd62ed3e") &&
      bytecode.contains("a9059cbb") &&
      bytecode.contains("095ea7b3") &&
      bytecode.contains("23b872dd")
  }

  /**
    * This method checks if there is a token name in bytecode, if not returns "Unknown"
    *
    * @return token's name
    */

  def getTokenName(): String = {

    try {

      val name = new String(
        Hex.decodeHex(
          StringUtils.substringBetween(bytecode, "81526020017f", "00").toCharArray
        ), "UTF-8")

      if (name == "address,bytes)"){
        "Unknown"
      } else {
        name
      }

    } catch {
      case np : NullPointerException => "Unknown"
      case de : DecoderException => "Unknown"
    }

  }

  /**
    * TThis method checks if there is a token symbol in bytecode, if not returns "Unknown"
    *
    * @return token's symbol
    */

  def getTokenSymbol(): String = {

    try {

      val temp = StringUtils.substringAfter(bytecode, "81526020017f")

      val symbol = new String(
        Hex.decodeHex(
          StringUtils.substringBetween(temp, "81526020017f", "00").toCharArray
        ), "UTF-8")

      if (name == "address,bytes)"){
        "Unknown"
      } else {
        symbol
      }

    } catch {
      case np : NullPointerException => "Unknown"
      case de : DecoderException => "Unknown"
    }

  }

}
