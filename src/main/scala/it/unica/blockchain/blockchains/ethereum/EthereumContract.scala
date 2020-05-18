package it.unica.blockchain.blockchains.ethereum

import java.util.Date

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils

import scala.util.matching.Regex


case class EthereumContract(
                             val name: String,
                             val address: EthereumAddress,
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
    *
    * @author Chessa Stefano Raimondo
    * @author Guria Marco
    * @author Manai Alessio
    * @author Speroni Alessio
    */

  def isERC20Compliant(): Boolean = {
    bytecode.contains("18160ddd") &&    //checks totalSupply() declaration
      bytecode.contains("70a08231") &&  //checks balanceOf(address) declaration
      bytecode.contains("dd62ed3e") &&  //checks allowance(address,address) declaration
      bytecode.contains("a9059cbb") &&  //checks transfer(address,uint256) declaration
      bytecode.contains("095ea7b3") &&  //checks approve(address,uint256) declaration
      bytecode.contains("23b872dd")     //checks transferFrom(address,address,uint256) declaration
  }

  def isERC721Compliant(): Boolean = {
    bytecode.contains("70a08231") && //checks balanceOf(address) declaration
      bytecode.contains("6352211e") && //checks ownerOf(uint256) declaration
      bytecode.contains("095ea7b3") && //checks approve(address,uint256) declaration
      bytecode.contains("081812fc") && //checks getApproved(uint256) declaration
      bytecode.contains("a22cb465") && //checks setApprovalForAll(address,bool) declaration
      bytecode.contains("e985e9c5") && //checks isApprovedForAll(address,address) declaration
      bytecode.contains("23b872dd") && //checks transferFrom(address,address,uint256) declaration
      bytecode.contains("42842e0e") && //checks safeTransferFrom(address,address,uint256) declaration
      bytecode.contains("b88d4fde") && //checks safeTransferFrom(address,address,uint256,bytes) declaration
      bytecode.contains("01ffc9a7") && //checks supportsInterface(bytes4) declaration
      bytecode.contains("150b7a02")    //checks onERC721Received(address,address,uint256,bytes) declaration
  }
}

object EthereumContract{

  def factory(name: String, address: EthereumAddress, hashOriginatingTx: String, isVerified: Boolean, verificationDate: Date, bytecode: String, sourceCode: String, searchForTokens: Boolean):EthereumContract ={
    var contract = EthereumContract(name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode)

    if (searchForTokens && contract.isERC20Compliant())
      new ERC20Token(name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode)
    else if (searchForTokens && contract.isERC721Compliant())
      new ERC721Token(name, address, hashOriginatingTx, isVerified, verificationDate, bytecode, sourceCode)
    else
      contract
  }
}
