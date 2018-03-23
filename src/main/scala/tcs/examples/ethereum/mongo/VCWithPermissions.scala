package tcs.examples.ethereum.mongo

import java.net.URLEncoder
import java.util.Date

import tcs.blockchain.BlockchainLib
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.HttpRequester


/**
  *
  * This script is an example of usage of the tool. If a local Parity node's JSON RPC Server is listening on port 8545,
  * and a MongoDB server is running, it will populate a Collection "VerifiedContracts" with:
  *
  * - contractAddress
  * - contractName
  * - date (to be fair, this is the date the block containing the tx that creates the contract was added to the
  *         blockchain, so there could be a little mismatch (at most a few days) compared to the real date.)
  * - sourceCode
  * - usesPermissions
  *
  *
  * @author Laerte
  * @author Luca
  */
object VCWithPermissions {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain("http://localhost:8545")
      .setStart(4900000).setEnd(5000000)
    val mongo = new DatabaseSettings("myDatabase")
    val verifiedContracts = new Collection("VerifiedContracts", mongo)

    blockchain.foreach(block => {
      if(block.number % 1000 == 0){
        println("Current block ->" + block.number)
      }
      val date = new Date(block.timeStamp.longValue()*1000)

      block.transactions.foreach(tx => {
        if (tx.creates != null && tx.verifiedContract == "true"){
          val format = new java.text.SimpleDateFormat("MM/dd/yyyy")


          val dateVerified = format.parse(tx.verificationDay)


          val sourceCode = getSourceCode(tx.creates)
          val list = List(
            ("contractAddress", tx.creates),
            ("contractName", tx.contractName),
            ("date", date),
            ("dateVerified", dateVerified),
            ("sourceCode", sourceCode),
            ("usesPermissions", findPermissions(sourceCode))
          )

          verifiedContracts.append(list)
        }

      })
    })

    verifiedContracts.close
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
  private def getSourceCode(address : String): String = {

    try {
      val content = HttpRequester.get("http://etherscan.io/address/" + address + "#code")
      //println(content)
      val strForContract = "Find Similiar Contracts"
      var sourceCode = content.substring(content.indexOf(strForContract)+strForContract.length)


      println("Getting source code for: " + address)
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
    * This method can be extended to implement smarter ways to find permissions, right now it just does a basic string
    * search.
    *
    * @param sourceCode
    * @return true if sourceCode contains permissions, false otherwise
    */
  private def findPermissions(sourceCode : String): Boolean = {

    if (sourceCode.contains("modifier onlyOwner()")) return true else return false

  }



}
