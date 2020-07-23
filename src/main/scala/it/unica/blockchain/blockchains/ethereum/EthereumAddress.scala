package it.unica.blockchain.blockchains.ethereum

/** Defines a standard Ethereum address
  *
  * @param address  An address
  */

class EthereumAddress (val address : String){

}

object EthereumAddress{

  /** this method verifies if the address passed respect the ethereum standard
    *
    * @param address    An address's string
    * @return if verified an EthereumAddress, else null
    */
  def factory(address :String): EthereumAddress ={
    val address_length = 42

    if(address != null &&
      address.length() == address_length &&
      address.startsWith("0x") &&
      address.matches("[a-zA-Z0-9]*")){
      new EthereumAddress(address.toLowerCase)
    }
    else
      null
  }
}