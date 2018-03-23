package tcs.blockchain.ethereum

import java.net.URLEncoder
import java.util.concurrent.CompletableFuture

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import org.web3j.protocol.core.methods.response.{EthGetTransactionReceipt, TransactionReceipt}
import org.web3j.protocol.http.HttpService
import shapeless.ops.nat.GT.>
import tcs.blockchain.Transaction
import tcs.utils.HttpRequester

/**
  * Defines an Ethereum Transaction
  *
  * @param hash transaction's hash
  * @param nonce transaction's nonce
  * @param blockHash hash of the block containing this transaction
  * @param blockNumber number of the block containing this transaction
  * @param transactionIndex index of the transaction inside its block
  * @param from from
  * @param to to
  * @param value transaction's value
  * @param gasPrice transaction's gas price
  * @param gas transaction's gas
  * @param input input of the transaction
  * @param creates true if this transaction creates a contract
  * @param publicKey transaction's public key
  * @param raw transaction's raw data
  * @param r r part
  * @param s s part
  * @param v v part
  */
case class EthereumTransaction(
                          hash: String,
                          nonce: BigInt,
                          blockHash: String,
                          blockNumber: BigInt,
                          transactionIndex: BigInt,
                          from: String,
                          to: String,
                          value: BigInt,
                          gasPrice: BigInt,
                          gas: BigInt,
                          input: String,
                          creates: String,
                          publicKey: String,
                          raw: String,
                          r: String,
                          s: String,
                          v: Int,
                          verifiedContract: String,
                          contractName: String,
                          verificationDay: String,
                          requestOpt: Option[Request[_, EthGetTransactionReceipt]]
                         ) extends Transaction {

  def getContractAddress(): Option[String] = {
    if (requestOpt.isDefined) {
      val request = requestOpt.get
      val response = request.send()
      val receiptOpt = response.getTransactionReceipt
      if (receiptOpt.isPresent && receiptOpt.get.getContractAddress != null) {
        return Some(receiptOpt.get.getContractAddress)
      }
    }
    None
  }
}

/**
  * Factories for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
  */
object EthereumTransaction{

  /**
    * Factory for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
    * Returns an EthereumTransaction, given it's Web3J representation
    *
    * @param tx Web3J representation of this transaction
    * @return new EthereumTransaction
    */
  def factory(tx: TransactionObject, receipt: Option[Request[_, EthGetTransactionReceipt]]): EthereumTransaction = {

    val creates = tx.getCreates()
    var verifiedContract, contractName, verificationDay = ""

    if (creates != null) {
      val (isVerified, name, verDay) = getVerifiedContract(creates)
      verifiedContract = isVerified
      contractName = name
      verificationDay = verDay
    }


    new EthereumTransaction(tx.getHash, tx.getNonce, tx.getBlockHash, tx.getBlockNumber, tx.getTransactionIndex,
                                   tx.getFrom, tx.getTo, tx.getValue, tx.getGasPrice, tx.getGas, tx.getInput,
                                   tx.getCreates, tx.getPublicKey, tx.getRaw, tx.getR, tx.getS, tx.getV,
                                   verifiedContract, contractName, verificationDay,
                                   receipt)
  }

  /**
    * This method parses HTML pages from etherscan.io to find whether or not a contract has been verified. If so, it
    * finds its name on the platform and its date of verification.
    *
    * This way of retrieving this data IS NOT OPTIMAL, but until etherscan.io adds a way to query the verified contracts
    * over their public API, this is the only way.
    *
    * NOTE: We tried to improve the robustness by iteratively looking for the contract date in subsequent pages (if not
    * found in the first one), because there is no way to control the query at
    * "https://etherscan.io/contractsVerified?cn=" to make a strict search.
    *
    * @author Laerte
    * @author Luca
    * @param contractAddress hex address of contract to check for verification
    * @return new Tuple3. In case of success, its fields will be populated with the aforementioned data. Otherwise,
    *         it returns 3 empty strings.
    */
  private def getVerifiedContract(contractAddress: String): (String, String, String) = {

    try {
      val content = HttpRequester.get("http://etherscan.io/address/" + contractAddress + "#code")
      //println(content)
      var isVerified = ""
      var name = ""
      var date = "01/01/1970"

      if (content.contains("<b>Contract Source Code Verified</b>")){
        isVerified = "true"
        val strForName = "<td>Contract Name:\n</td>\n<td>"
        name = content.substring(content.indexOf(strForName)+strForName.length)
        name = name.substring(0, name.indexOf("<"))


        val datePage = HttpRequester.get("https://etherscan.io/contractsVerified?cn=" + URLEncoder.encode(name, "UTF-8"))


        val indexOfContract = datePage.indexOf(contractAddress)


        if (indexOfContract == -1){

          var numPages = datePage.substring(datePage.indexOf("</b> of <b>") + "</b> of <b>".length)

          numPages = numPages.substring(0, numPages.indexOf("<"))

          val n = numPages.toFloat

          var currPage = ""
          var currIndexOfContract = -1
          var i = 2

          while  (i<=n && currIndexOfContract == -1){ //This for keeps looking for the contract in pages further than
                                                      // the first one
            currPage = HttpRequester.get("https://etherscan.io/contractsVerified"+ i + "?cn=" + URLEncoder.encode(name, "UTF-8"))

            currIndexOfContract = currPage.indexOf(contractAddress)

            if (currIndexOfContract != -1){
              println("Contract found at page: " + i)

              date = currPage.substring(datePage.indexOf(contractAddress) + contractAddress.length)
              date = date.substring(date.indexOf("Ether</td><td>") + "Ether</td><td>".length)
              date = date.substring(date.indexOf("<td>") + 4)
              date = date.substring(0, date.indexOf("<"))
            }

            i+=1

          }
        }
        else{

          date = datePage.substring(datePage.indexOf(contractAddress) + contractAddress.length)
          date = date.substring(date.indexOf("Ether</td><td>") + "Ether</td><td>".length)
          date = date.substring(date.indexOf("<td>") + 4)
          date = date.substring(0, date.indexOf("<"))
        }

      }

      return (isVerified, name, date)

    } catch {
      case ioe: java.io.IOException => {ioe.printStackTrace(); return ("", "", "")}
      case ste: java.net.SocketTimeoutException => {ste.printStackTrace(); return ("", "", "")}
      case e: Exception => {e.printStackTrace(); return ("", "", "")}
    }
  }


}