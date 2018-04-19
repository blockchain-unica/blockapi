package tcs.blockchain.ethereum

import java.math.BigInteger
import java.net.URLEncoder
import java.util.Date

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameterName, Request}
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import org.web3j.protocol.core.methods.response.{EthGetTransactionReceipt, TransactionReceipt}
import tcs.blockchain.Transaction
import tcs.utils.HttpRequester
import tcs.utils.Etherscan.getSourceCodeFromEtherscan


/**
  * Defines an Ethereum Transaction
  *
  * @param hash transaction's hash
  * @param date date in which the transaction has been published (extracted from the containing block)
  * @param nonce transaction's nonce
  * @param blockHash hash of the block containing this transaction
  * @param blockHeight number of the block containing this transaction
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
                                override val hash: String,
                                override val date: Date,

                                val nonce: BigInt,
                                val blockHash: String,
                                val blockHeight: BigInt,
                                val transactionIndex: BigInt,
                                val from: String,
                                val to: String,
                                val value: BigInt,
                                val gasPrice: BigInt,
                                val gas: BigInt,
                                val input: String,
                                val addressCreated: String,
                                val publicKey: String,
                                val raw: String,
                                val r: String,
                                val s: String,
                                val v: Int,

                                val contract : EthereumContract,
                                val requestOpt: Option[Request[_, EthGetTransactionReceipt]]
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

  def hasContract : Boolean = {
    return contract != null
  }
}

/**
  * Factories for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
  */
object EthereumTransaction{

  private var web3j : Web3j = null

  /**
    * Factory for [[tcs.blockchain.ethereum.EthereumTransaction]] instances
    * Returns an EthereumTransaction, given it's Web3J representation
    *
    * @param tx Web3J representation of this transaction
    * @return new EthereumTransaction
    */
  def factory(tx: TransactionObject, txDate: Date, receipt: Option[Request[_, EthGetTransactionReceipt]], retrieveVerifiedContracts: Boolean, web3j: Web3j): EthereumTransaction = {

    this.web3j = web3j

    // If the transaction creates a contract, initialize it.
    var contract : EthereumContract = null
    if (tx.getCreates() != null) {
      if(retrieveVerifiedContracts) {
        contract = getVerifiedContract(tx)
      } else {
        contract = getContract(tx)
      }
    }

    new EthereumTransaction(tx.getHash, txDate, tx.getNonce, tx.getBlockHash, tx.getBlockNumber, tx.getTransactionIndex,
                                   tx.getFrom, tx.getTo, tx.getValue, tx.getGasPrice, tx.getGas, tx.getInput,
                                   tx.getCreates, tx.getPublicKey, tx.getRaw, tx.getR, tx.getS, tx.getV,
                                   contract, receipt)
  }


  /** */
  def getContract(tx: TransactionObject): EthereumContract = {
    new EthereumContract("", tx.getCreates, tx.getHash, false, null, getContractBytecode(tx.getCreates), null)
  }

  def getConctractBalance(contractAddress : String) : BigInteger = {
    this.web3j.ethGetBalance(contractAddress,DefaultBlockParameterName.LATEST).send().getBalance
  }

  /**
    * This method parses HTML pages from etherscan.io to find whether or not a contract has been verified.
    * If so, it finds its name on the platform and its date of verification, then creates an EthereumContract.
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
    * @param tx Web3J representation of the transaction containing data about the contract
    * @return An Ethereum contract. In case of success, its fields will be populated with the aforementioned data.
    *         Otherwise it returns a null value.
    */
  private def getVerifiedContract(tx: TransactionObject): EthereumContract = {

    var contract : EthereumContract = null

    var isVerified = false
    var name = ""
    val format = new java.text.SimpleDateFormat("MM/dd/yyyy")
    var date = format.parse("01/01/1970")

    try {
      val content = HttpRequester.get("http://etherscan.io/address/" + tx.getCreates + "#code")
      //println(content)

      if (content.contains("<b>Contract Source Code Verified</b>")){
        isVerified = true

        // Fetches the contract name
        val strForName = "<td>Contract Name:\n</td>\n<td>"
        name = content.substring(content.indexOf(strForName)+strForName.length)
        name = name.substring(0, name.indexOf("<"))

        // Fetches the date in which the contract has been verified
        val datePage = HttpRequester.get("https://etherscan.io/contractsVerified?cn=" + URLEncoder.encode(name, "UTF-8"))
        val indexOfContract = datePage.indexOf(tx.getCreates)

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
            currIndexOfContract = currPage.indexOf(tx.getCreates)

            // Contract found at page i
            if (currIndexOfContract != -1){
              date = getDateFromHtml(currPage, tx.getCreates, format)
            }

            i+=1

          }
        }

        // Date found
        else{
          date = getDateFromHtml(datePage, tx.getCreates, format)
        }
      }

      return new EthereumContract(name, tx.getCreates, tx.getHash, isVerified, date, getContractBytecode(tx.getCreates), getSourceCodeFromEtherscan(tx.getCreates))

    } catch {
      case ioe: java.io.IOException => {ioe.printStackTrace(); return contract}
      case ste: java.net.SocketTimeoutException => {ste.printStackTrace(); return contract}
      case e: Exception => {e.printStackTrace(); return contract}
    }
  }


  private def getDateFromHtml(page : String, contractAddress: String, format : java.text.SimpleDateFormat) : Date = {

    var stringDate = page.substring(page.indexOf(contractAddress) + contractAddress.length)
    stringDate = stringDate.substring(stringDate.indexOf("Ether</td><td>") + "Ether</td><td>".length)
    stringDate = stringDate.substring(stringDate.indexOf("<td>") + 4)
    stringDate = stringDate.substring(0, stringDate.indexOf("<"))
    return format.parse(stringDate)
  }


  private def getContractBytecode(contractAddress : String) : String = {
    this.web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send().getCode
  }
}