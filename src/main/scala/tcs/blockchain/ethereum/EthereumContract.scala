package tcs.blockchain.ethereum

import java.util.Date


case class EthereumContract(
                           val name : String,
                           val address : String,
                           val hashOriginatingTx : String,

                           val isVerified : Boolean,
                           val verificationDate : Date,

                           val bytecode : String,
                           val sourceCode : String
  ){

  val usesPermissions = isVerified && checkPermissions


  /**
    * This method can be extended to implement smarter ways to find permissions,
    * right now it just does a basic string search.
    *
    * @return true if sourceCode contains permissions, false otherwise
    */
  private def checkPermissions() : Boolean = {

    return sourceCode.contains("modifier onlyOwner()")
  }
}
