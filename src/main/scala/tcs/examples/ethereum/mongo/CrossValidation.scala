package tcs.examples.ethereum.mongo

import java.util.{LinkedList, Map}

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.Etherscan

object CrossValidation {
  def main(args: Array[String]): Unit = {
    val startBlock = 700000
    val endBlock = 700300
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val myBlockchain1 = new Collection("ethValidation1", mongo)
    val myBlockchain2 = new Collection("ethValidation2", mongo)

    try {
      // Chiedere se gli estremi dei blocchi devono essere passati in input o hardcoded
      blockchain.start(startBlock).end(endBlock).foreach(block => {
        if (block.height % 100 == 0) {
          println("Current block -> " + block.height)
        }

        block.txs.foreach(tx => {
          val list = List(
            ("txHash", tx.hash),
            ("date", block.date),
            ("value", tx.value.doubleValue() / weiIntoEth.doubleValue()),
            ("hasContract", tx.hasContract)
          )
          myBlockchain1.append(list)
        })
      })

      myBlockchain1.close

      for (ind <- startBlock to endBlock) {
        val block = Etherscan.getBlock(ind.toHexString)

        if (ind % 100 == 0) {
          println("Current block -> " + ind)
        }

        if (!(block.isEmpty) && block.get("transactions") != "Empty") {
          val txs = block.get("transactions").asInstanceOf[LinkedList[Map[String, String]]]

          txs.forEach((tx: Map[String, String]) => {
            val list = List(
              ("txHash", tx.get("hash")),
              ("date", block.get("timestamp")) //,
              // ("value", java.lang.Long.decode(tx.get("value").toString))//.toDouble / weiIntoEth.doubleValue())//,
              //("hasContract", tx.hasContract)   Non c'è un campo apposito per questo
            )
            myBlockchain2.append(list)
          })
        }
      }
      myBlockchain2.close
    } catch {
      case e: Exception => {
        e.printStackTrace();
        println("Parameter error")
      }
    }
  }
}
