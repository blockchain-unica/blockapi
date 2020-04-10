package it.unica.blockchain.externaldata.contracts

import java.net.URLEncoder
import java.util.Date

import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import it.unica.blockchain.utils.httprequester.HttpRequester

object Etherscan {

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
  def getVerifiedContractFromEtherscan(tx: TransactionObject): (String, Date, String, Boolean) = {

    var isVerified = false
    var name, source = ""
    var format = new java.text.SimpleDateFormat("MM/dd/yyyy")
    var date = format.parse("01/01/1970")

    val content = HttpRequester.get("http://etherscan.io/address/" + tx.getCreates + "#code")
    //println(content)

    if (content.contains("Contract Source Code Verified")){
      isVerified = true

      // Fetches the contract name
      val strForName = "Contract Name:"
      name = content.substring(content.indexOf(strForName)+strForName.length)
      name = name.substring(name.indexOf("<span"))
      name = name.substring(name.indexOf(">") +1)
      name = name.substring(0, name.indexOf("<"))

      // Fetches the contract bytecode
      val strForBytecode = "<div id='verifiedbytecode2'>"
      source = content.substring(content.indexOf(strForBytecode)+strForBytecode.length)
      source = source.substring(0, source.indexOf("<"))


      // Fetches the date in which the contract has been verified
      val datePage = HttpRequester.get("https://etherscan.io/contractsVerified?cn=" + URLEncoder.encode(name, "UTF-8"))
      val indexOfContract = datePage.indexOf(tx.getCreates)

      // The date is not written in the current page; inspects next pages
      if (indexOfContract == -1){

        // Retrives the total number of pages
        var numPages = datePage.substring(datePage.indexOf("1</strong> of") + "1</strong> of".length)
        numPages = numPages.substring(numPages.indexOf(">")+1)
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

    return (name, date, source, isVerified)

  }


  private def getDateFromHtml(page : String, contractAddress: String, format : java.text.SimpleDateFormat) : Date = {

    var stringDate = page.substring(page.indexOf(contractAddress) + contractAddress.length)
    stringDate = stringDate.substring(stringDate.indexOf("Ether</td><td>") + "Ether</td><td>".length)
    stringDate = stringDate.substring(stringDate.indexOf("<td>") + 4)
    stringDate = stringDate.substring(0, stringDate.indexOf("<"))
    return format.parse(stringDate)
  }


  /**
    *
    * This method of fetching the contract's source code is NOT optimal, but until etherscan.io extends its API to
    * verified contracts, this is the only way.
    * If this doesn't work, it could be because etherscan.io changed their html, so this method is really not robust to
    * changes.
    *
    * @param address
    * @return new String whose content is the contract's source code
    */
  private def getSourceCodeFromEtherscan(address : String): String = {

    try {
      val content = HttpRequester.get("http://etherscan.io/address/" + address + "#code")
      //println(content)
      val strForContract = "Find Similiar Contracts"
      var sourceCode = content.substring(content.indexOf(strForContract)+strForContract.length)


      //println("Getting source code for: " + address)
      sourceCode = sourceCode.substring(sourceCode.indexOf("<pre") + 4)
      sourceCode = sourceCode.substring(sourceCode.indexOf(">") + 1, sourceCode.indexOf("</pre><br><script"))

      return sourceCode
    } catch {
      case ioe: java.io.IOException => {ioe.printStackTrace(); return ""}
      case ste: java.net.SocketTimeoutException => {ste.printStackTrace(); return ""}
      case e: Exception => {e.printStackTrace(); return ""}
    }
  }
}
