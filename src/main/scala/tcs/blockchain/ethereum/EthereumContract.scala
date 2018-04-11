package tcs.blockchain.ethereum

import java.util.Date


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
  val isERC20Compliant = iserc20Compliant

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
    * This method check if the contract is ERC20 compliant analyzing the bytecode
    * source: https://ethereum.stackexchange.com/questions/38381/how-can-i-identify-that-transaction-is-erc20-token-creation-contract?answertab=oldest#tab-top
    *
    * @return true if the contract is ERC20 compliant, false otherwise
    */

  private def iserc20Compliant(): Boolean = {
    return bytecode.contains("18160ddd") &&
      bytecode.contains("70a08231") &&
      bytecode.contains("dd62ed3e") &&
      bytecode.contains("a9059cbb") &&
      bytecode.contains("095ea7b3") &&
      bytecode.contains("23b872dd")
  }

}
