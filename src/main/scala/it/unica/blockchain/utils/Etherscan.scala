package it.unica.blockchain.utils

import java.util

import com.codesnippets4all.json.parsers.JsonParserFactory
import it.unica.blockchain.utils.httprequester.HttpRequester
import play.api.libs.json.Json
import scalaj.http.Http

package object Etherscan {
  def apiKey = "Insert apikey"
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
  def getSourceCodeFromEtherscan(address : String): String = {

    val content = Http("https://api.etherscan.io/api?module=contract&action=getsourcecode&address=" + address + "&apikey=" + apiKey).asString.body
    val json = Json.parse(content);
    val sourceCode = (json \ "result" \ "0" \ "SourceCode").get.as[String];

    return sourceCode
  }

  /**
    * @param blockAddress the block address
    * @param retry number of attempt already done
    * @return map describing block fields
    */
  def getBlock(blockAddress: String, retry: Int = 0): util.Map[String,Any] = {
    waitForRequest()
    try {
      //The json parsedr fails when it finds an empty array, so it is replaced by the string "Empty"
      val content = HttpRequester.get("https://api.etherscan.io/api?module=proxy&action=eth_getBlockByNumber&tag=" +
        blockAddress +"&boolean=true&apikey=" + apiKey).replaceAll("\\Q[]\\E","\"Empty\"")

      val map = JsonParserFactory.getInstance.newJsonParser().parseJson(content)
      val block = map.get("result").asInstanceOf[util.Map[String,Any]]

      val transactions = block.get("transactions")
      transactions match {
        case "Empty" => {}
        case list => {
          val txs = list.asInstanceOf[util.ArrayList[util.Map[String, Any]]]
          txs.forEach(tx => {
            if (tx.get("to") == "null" && transactionHasContract(tx.get("hash").toString)) {
              tx.put("hasContract", true)
            }
            else {
              tx.put("hasContract", false)
            }
            block.replace("transactions", txs)
          })
        }
      }
      return block
    }
    catch {
      //Request error, retry until 5 times
      case ioe: java.io.IOException => {
        if (retry < 5) {
          return getBlock(blockAddress, retry + 1)
        }
        else {
          println("Errors while getting block num: " + blockAddress)
          ioe.printStackTrace()
          return null
        }
      }
      //Json parse error
      case e: Exception => {
        println("Errors in json parsing of block num: " + blockAddress)
        e.printStackTrace()
        return null
      }
    }
  }

  /**
    * @param transactionAddress the transaction address
    * @param retry number of attempt already done
    * @return true if transaction has contract
    */
  def transactionHasContract(transactionAddress: String, retry: Int = 0): Boolean = {
    waitForRequest()
    try {
      val content = HttpRequester.get("https://api.etherscan.io/api?module=proxy&action=eth_getTransactionReceipt&txhash="
        + transactionAddress + "&apikey=" + apiKey).replaceAll("\\Q[]\\E","\"Empty\"")

      val map = JsonParserFactory.getInstance.newJsonParser().parseJson(content)

      return (map.get("contractAddress") != "null")
    }
    catch {
      //Request error, retry until 5 times
      case ioe: java.io.IOException => {
        if (retry < 5) {
          return transactionHasContract(transactionAddress, retry + 1)
        }
        else {
          println("Request error while getting transaction num: " + transactionAddress)
          ioe.printStackTrace()
          return false
        }
      }
      //Json parse error
      case e: Exception => {
        println("Errors in json parsing of transaction num: " + transactionAddress)
        e.printStackTrace()
        return false
      }
    }
  }

  //Busy waiting to not exceed api rate limit (5 request/sec)
  def waitForRequest() = {
    Thread.sleep(200)
  }
}
