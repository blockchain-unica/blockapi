package it.unica.blockchain.blockchains.ethereum.tokenUtils

/** Defines the types of Token actually supported.
  * None doesn't reppresent any token.
  */

object TokenType extends Enumeration {
  type TokenType = Value
  val ERC20, ERC721, None = Value
}
