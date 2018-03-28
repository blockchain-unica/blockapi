package tcs.blockchain.ethereum

import java.net.URLEncoder
import java.util.Date

import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import org.web3j.protocol.core.methods.response.{EthGetTransactionReceipt, TransactionReceipt}
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
  * @param addressCreated Address of the created contract (if this transaction creates a contract)
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
                                addressCreated: String,
                                publicKey: String,
                                raw: String,
                                r: String,
                                s: String,
                                v: Int,
                                verifiedContract: Boolean,
                                contractName: String,
                                verificationDay: Date,
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

    var verifiedContract = false
    var contractName = ""
    val format = new java.text.SimpleDateFormat("MM/dd/yyyy")
    var verificationDay = format.parse("01/01/1970")

    // Retrieve the address of the created contract, if the transaction creates a contract.
    if (tx.getCreates() != null) {
      val (isVerified, name, verDay) = getVerifiedContract(tx.getCreates())
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
  private def getVerifiedContract(contractAddress: String): (Boolean, String, Date) = {

    var isVerified = false
    var name = ""
    val format = new java.text.SimpleDateFormat("MM/dd/yyyy")
    var date = format.parse("01/01/1970")

    try {
      val content = HttpRequester.get("http://etherscan.io/address/" + contractAddress + "#code")
      //println(content)

      if (content.contains("<b>Contract Source Code Verified</b>")){
        isVerified = true

        // Fetches the contract name
        val strForName = "<td>Contract Name:\n</td>\n<td>"
        name = content.substring(content.indexOf(strForName)+strForName.length)
        name = name.substring(0, name.indexOf("<"))

        // Fetches the date in which the contract has been verified
        val datePage = HttpRequester.get("https://etherscan.io/contractsVerified?cn=" + URLEncoder.encode(name, "UTF-8"))
        val indexOfContract = datePage.indexOf(contractAddress)

        // The date is not written in the current page; inspects next pages
        if (indexOfContract == -1){

          // Retrives the total number of pages
          var numPages = datePage.substring(datePage.indexOf("</b> of <b>") + "</b> of <b>".length)
          numPages = numPages.substring(0, numPages.indexOf("<"))
          val n = numPages.toInt

          var currPage = ""
          var currIndexOfContract = -1
          var i = 2

          // This for keeps looking for the contract in pages further than the first one
          while  (i<=n && currIndexOfContract == -1){
            currPage = HttpRequester.get("https://etherscan.io/contractsVerified/"+ i + "?cn=" + URLEncoder.encode(name, "UTF-8"))
            currIndexOfContract = currPage.indexOf(contractAddress)

            // Contract found at page i
            if (currIndexOfContract != -1){
              date = getDateFromHtml(currPage, contractAddress, format)
            }

            i+=1

          }
        }

        // Date found
        else{
          date = getDateFromHtml(datePage, contractAddress, format)
        }
      }

      return (isVerified, name, date)

    } catch {
      case ioe: java.io.IOException => {ioe.printStackTrace(); return (false, name, date)}
      case ste: java.net.SocketTimeoutException => {ste.printStackTrace(); return (false, name, date)}
      case e: Exception => {e.printStackTrace(); return (false, name, date)}
    }
  }


  private def getDateFromHtml(page : String, contractAddress: String, format : java.text.SimpleDateFormat) : Date = {

    var stringDate = page.substring(page.indexOf(contractAddress) + contractAddress.length)
    stringDate = stringDate.substring(stringDate.indexOf("Ether</td><td>") + "Ether</td><td>".length)
    stringDate = stringDate.substring(stringDate.indexOf("<td>") + 4)
    stringDate = stringDate.substring(0, stringDate.indexOf("<"))
    return format.parse(stringDate)
  }


}