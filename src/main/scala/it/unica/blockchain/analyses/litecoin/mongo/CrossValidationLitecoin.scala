package it.unica.blockchain.analyses.litecoin.mongo

import java.sql.Date
import java.text.SimpleDateFormat

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import play.api.libs.json.{JsArray, JsValue, Json}
import scalaj.http.Http

object CrossValidationLitecoin {
  def main(args: Array[String]): Unit = {
    val initialBlock: Int = 1447684
    val finalBlock: Int = 1447685
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
      val jsonString = Http("https://chain.so/api/v2/get_blockhash/LTC/" + blockId).timeout(1000000000, 1000000000).asString.body
      val jsonObject = Json.parse(jsonString)
      val blockHash = (jsonObject \ "data" \ "blockhash").as[JsValue].toString().substring(1, (jsonObject \ "data" \ "blockhash").as[JsValue].toString().size-1)
      val txJsonString = Http("https://chain.so/api/v2/get_block/LTC/" + blockHash).timeout(1000000000, 1000000000).asString.body
      val txHashJsonObject = Json.parse(txJsonString)
      val txHashArray = (txHashJsonObject \ "data" \ "txs").as[JsArray]
      println("Block Hash: " + blockHash)
      var counter = 0

      for (i <- 0 to txHashArray.value.size-1) {
        counter += 1
        val txHashRequest = Http("https://chain.so/api/v2/tx/LTC/" + txHashArray(i).toString().substring(1, txHashArray(i).toString().size-1)).timeout(1000000000, 1000000000).asString.body
        val txJsonObject = Json.parse(txHashRequest)
        val inputCount = (txJsonObject \ "data" \ "inputs").as[JsArray].value.size
        val outputCount = (txJsonObject \ "data" \ "outputs").as[JsArray].value.size
        val outputSum:Int = (((txJsonObject \ "data" \ "sent_value").as[JsValue].toString().substring(1, (txJsonObject \ "data" \ "sent_value").as[JsValue].toString().length-1).toDouble -
          (txJsonObject \ "data" \ "fee").as[JsValue].toString().substring(1, (txJsonObject \ "data" \ "fee").as[JsValue].toString().length-1).toDouble) * 100000000).toInt
        val date = new Date(((txJsonObject \ "data" \ "time").as[Long])*1000)
        val sfd:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0000")
        println(sfd.format(date))



      }
      println(counter)





    }


  }
}
