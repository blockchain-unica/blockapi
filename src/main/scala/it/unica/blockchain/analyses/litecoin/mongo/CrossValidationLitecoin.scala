package it.unica.blockchain.analyses.litecoin.mongo

import java.sql.Date
import java.text.SimpleDateFormat
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import play.api.libs.json.{JsArray, JsValue, Json}
import scalaj.http.Http

/**The script uses two methods to get informations from both Litecoin blockchain tool
  * and Litecoin blockchain explorer, and updates a collection on MongoDB with the obtained values.
  *
  * To be more specific:
  * Method getDataFromTool explores the Litecoin blockchain downloaded from the tool and stores the
  * values in a collection.
  * Method getDataFromChainSo performs requests with Http protocol to get the values from chain.so
  * and stores them in a collection.
  *
  * The stored values are, for each transaction:
  *   -Hash
  *   -Date
  *   -Number of inputs
  *   -Number of outputs
  *   -Output value of the whole transaction
  *
  * @author Daniele Sanna
  * @author Giovanni Usai
   */



object CrossValidationLitecoin {
  def main(args: Array[String]): Unit = {
    /*Initialization of parameters to scan a range of blocks and to create a database on MongoDB*/
    val initialBlock: Int = 1443231
    val finalBlock: Int = 1443330
    val dbMongo = new DatabaseSettings("litecoinDB")

    getDataFromTool(initialBlock, finalBlock, dbMongo)
    getDataFromChainSo(initialBlock, finalBlock, dbMongo)

  }

  def getDataFromTool(initialBlock: Int, finalBlock: Int, dbMongo: DatabaseSettings): Unit = {

    /*Initialization of needed parameters to scan the blocks*/
    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9333", MainNet))
    val toolBlockchain = new Collection("litecoinToolValidation", dbMongo)
    var blockId: Int = initialBlock

    try {
      /*Scan the range of blocks defined by initialBlock and finalBlock*/
      blockchain.start(initialBlock).end(finalBlock).foreach(block => {
        blockId = block.height.intValue()
        println("Current block ID: " + blockId)

        /*For each transaction in each block stores the asked values in a list and updates the collection with
        * the obtained values*/
        block.txs.foreach(tx => {
          val list = List(
            ("hash", tx.hash),
            ("date", tx.date),
            ("inputCount", tx.inputs.length),
            ("outputCount", tx.outputs.length),
            ("outputValue", tx.getOutputsSum())
          )
          toolBlockchain.append(list)
        })
      })
      toolBlockchain.close

    } catch {
      /*Throws an exception if there are errors while processing a block*/
      case e: Exception => {
        toolBlockchain.close
        println("Error while proessing block " + blockId)
        e.printStackTrace()
        getDataFromTool(blockId + 1, finalBlock, dbMongo)
      }
    }
  }

  def getDataFromChainSo(initialBlock: Int, finalBlock: Int, dbMongo: DatabaseSettings): Unit = {
    val explorerBlockchain = new Collection("litecoinExplorerValidation", dbMongo)
    var blockId:Int = initialBlock

    try {
      /*Scan the range of blocks defined by initialBlock and finalBlock*/
      for (blockId <- initialBlock to finalBlock) {
        var jsonString = " "
        var txJsonString = " "

        /*Gets the block hash by performing a Http request using the number of the block.
      * The do-while construct is used to avoid being kicked from the domain because of the
      * "Too many requests" error.*/
        do {
          jsonString = Http("https://chain.so/api/v2/get_blockhash/LTC/" + blockId).timeout(1000000000, 1000000000).asString.body
          sleep(1000)
        } while (jsonString.contains("Too"))

        val jsonObject = Json.parse(jsonString)
        val blockHash = (jsonObject \ "data" \ "blockhash").as[JsValue].toString().substring(1, (jsonObject \ "data" \ "blockhash").as[JsValue].toString().size - 1)

        /*Gets the set of transaction's hash in a block and stores it in an array*/
        do {
          txJsonString = Http("https://chain.so/api/v2/get_block/LTC/" + blockHash).timeout(1000000000, 1000000000).asString.body
          sleep(1000)
        } while (txJsonString.contains("Too"))

        val txHashJsonObject = Json.parse(txJsonString)
        val txHashArray = (txHashJsonObject \ "data" \ "txs").as[JsArray]

        /*Makes a request for the values for each transaction, stores them in a list and updates the collection*/
        for (i <- 0 to txHashArray.value.size - 1) {
          val sfd: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0000")
          var txHashRequest = " "
          do {
            txHashRequest = Http("https://chain.so/api/v2/tx/LTC/" + txHashArray(i).toString().substring(1, txHashArray(i).toString().size - 1)).timeout(1000000000, 1000000000).asString.body
            sleep(1000)
          } while (txHashRequest.contains("Too"))

          val txJsonObject = Json.parse(txHashRequest)
          val explorerList = List(
            ("hash", txHashArray(i).toString().substring(1, txHashArray(i).toString().size - 1)),
            ("date", sfd.parse(sfd.format(new Date(((txJsonObject \ "data" \ "time").as[Long]) * 1000)))),
            ("inputCount", (txJsonObject \ "data" \ "inputs").as[JsArray].value.size),
            ("outputCount", (txJsonObject \ "data" \ "outputs").as[JsArray].value.size),
            ("outputValue", ((txJsonObject \ "data" \ "sent_value").as[JsValue].toString().substring(1, (txJsonObject \ "data" \ "sent_value").as[JsValue].toString().length - 1).toDouble * 100000000L) -
              ((txJsonObject \ "data" \ "fee").as[JsValue].toString().substring(1, (txJsonObject \ "data" \ "fee").as[JsValue].toString().length - 1).toDouble * 100000000L))
          )

          explorerBlockchain.append(explorerList)
        }
      }
      explorerBlockchain.close
    }catch {
      /*Throws an exception if there are errors while processing a block*/
      case e: Exception => {
        explorerBlockchain.close
        println("Error while proessing block " + blockId)
        e.printStackTrace()
        getDataFromTool(blockId + 1, finalBlock, dbMongo)
      }
    }

  }
  /*Definition of a sleep function used to make requests with a delay to minimize the number of "Too many requests" error
  * from chain.so*/
  def sleep(time: Long): Unit = Thread.sleep(time)
}
