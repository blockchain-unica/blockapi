package it.unica.blockchain.analyses.litecoin.mongo

import java.sql.Date
import java.text.SimpleDateFormat
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import play.api.libs.json.{JsArray, JsValue, Json}
import scalaj.http.Http

/** **/


object CrossValidationLitecoin {
  def main(args: Array[String]): Unit = {
    val initialBlock: Int = 1443231
    val finalBlock: Int = 1443330
    val dbMongo = new DatabaseSettings("litecoinDB")

    getDataFromTool(initialBlock, finalBlock, dbMongo)
    getDataFromChainSo(initialBlock, finalBlock, dbMongo)

  }

  def getDataFromTool(initialBlock: Int, finalBlock: Int, dbMongo: DatabaseSettings): Unit = {
    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9333", MainNet))
    val toolBlockchain = new Collection("litecoinToolValidation", dbMongo)
    var blockId: Int = initialBlock

    try {
      blockchain.start(initialBlock).end(finalBlock).foreach(block => {
        blockId = block.height.intValue()
        println("Current block ID: " + blockId)

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

    for (blockId <- initialBlock to finalBlock) {
      var jsonString = " "
      var txJsonString = " "
      do {
        jsonString = Http("https://chain.so/api/v2/get_blockhash/LTC/" + blockId).timeout(1000000000, 1000000000).asString.body
        sleep(1000)
      } while (jsonString.contains("Too"))
      val jsonObject = Json.parse(jsonString)
      val blockHash = (jsonObject \ "data" \ "blockhash").as[JsValue].toString().substring(1, (jsonObject \ "data" \ "blockhash").as[JsValue].toString().size-1)
      do {
        txJsonString = Http("https://chain.so/api/v2/get_block/LTC/" + blockHash).timeout(1000000000, 1000000000).asString.body
        sleep(1000)
      } while (txJsonString.contains("Too"))

      val txHashJsonObject = Json.parse(txJsonString)
      val txHashArray = (txHashJsonObject \ "data" \ "txs").as[JsArray]

      for (i <- 0 to txHashArray.value.size-1) {
        val sfd:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0000")
        var txHashRequest = " "
        do {
          txHashRequest = Http("https://chain.so/api/v2/tx/LTC/" + txHashArray(i).toString().substring(1, txHashArray(i).toString().size - 1)).timeout(1000000000, 1000000000).asString.body
          sleep(1000)
        } while (txHashRequest.contains("Too"))

        val txJsonObject = Json.parse(txHashRequest)
        val explorerList = List (
          ("hash", txHashArray(i).toString().substring(1, txHashArray(i).toString().size - 1)),
          ("date", sfd.parse(sfd.format(new Date(((txJsonObject \ "data" \ "time").as[Long])*1000)))),
          ("inputCount", (txJsonObject \ "data" \ "inputs").as[JsArray].value.size),
          ("outputCount", (txJsonObject \ "data" \ "outputs").as[JsArray].value.size),
          ("outputValue", ((txJsonObject \ "data" \ "sent_value").as[JsValue].toString().substring(1, (txJsonObject \ "data" \ "sent_value").as[JsValue].toString().length-1).toDouble* 100000000L) -
            ((txJsonObject \ "data" \ "fee").as[JsValue].toString().substring(1, (txJsonObject \ "data" \ "fee").as[JsValue].toString().length-1).toDouble * 100000000L))
        )

        explorerBlockchain.append(explorerList)

      }
    }
    explorerBlockchain.close

  }
  def sleep(time: Long): Unit = Thread.sleep(time)
}
