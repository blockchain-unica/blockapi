package tcs.utils

import com.codesnippets4all.json.parsers.JsonParserFactory

import com.codesnippets4all.json.config.handlers.ValidationConfigType
import java.util.Map
import java.util.LinkedList
import scalaj.http.Http
import tcs.custom.ethereum.Utils

package object Etherscan {
  def apiKey = "apiKey"
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

  /**
    * @param blockAddress the block address
    * @return json structure of the block
    */
  def getBlock(blockAddress: String): Map[String,String] = {
    try {
      //The json parser fails when it finds an empty array
      val content = HttpRequester.get("https://api.etherscan.io/api?module=proxy&action=eth_getBlockByNumber&tag=" +
        blockAddress +"&boolean=true&apikey==" + apiKey).replaceAll("\\Q[]\\E","\"Empty\"")
      println(content)

      val factory = JsonParserFactory.getInstance
      val parser = factory.newJsonParser(ValidationConfigType.JSON)
      val map = parser.parseJson(content)
      val block = map.get("result").asInstanceOf[java.util.Map[String,String]]

      return block
    }
    catch {
      case ioe: java.io.IOException => {
        ioe.printStackTrace(); return null
      }
      case ste: java.net.SocketTimeoutException => {
        ste.printStackTrace(); return null
      }
      case e: Exception => {
        e.printStackTrace(); return null
      }
    }
  }
}
