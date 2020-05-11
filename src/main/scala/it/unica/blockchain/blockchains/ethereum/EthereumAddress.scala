package it.unica.blockchain.blockchains.ethereum

class EthereumAddress (val address : String){

}

object EthereumAddress{

  def factory(address :String): EthereumAddress ={
    val address_length = 42;

    if(address.length() == address_length &&
      address.startsWith("0x") &&
      address.matches("[a-zA-Z0-9]*")){
      new EthereumAddress(address)
    }
    else
      null
  }
}