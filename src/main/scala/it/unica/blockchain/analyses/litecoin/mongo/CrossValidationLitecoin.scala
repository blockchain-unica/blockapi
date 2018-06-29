package it.unica.blockchain.analyses.litecoin.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.litecoin.{LitecoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

object CrossValidationLitecoin {
  def main(args: Array[String]): Unit= {
    val initialBlock:Int = 1447684
    val finalBlock:Int = 1447688
    val dbMongo = new DatabaseSettings("litecoinDB")

    getDataFromTool(initialBlock, finalBlock, dbMongo)

  }

  def getDataFromTool(initialBlock:Int, finalBlock:Int, dbMongo:DatabaseSettings): Unit= {
    val blockchain = BlockchainLib.getLitecoinBlockchain(new LitecoinSettings("user", "password", "9333", MainNet))
    val toolBlockchain = new Collection("litecoinToolValidation", dbMongo)
    var blockId:Int = initialBlock

    blockchain.start(initialBlock).end(finalBlock).foreach(block => {
      blockId = block.height.intValue()
      println("Current block ID: " + blockId)

      block.txs.foreach(tx => {
        val list = List (
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




  }

}
