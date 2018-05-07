package tcs.examples.ethereum.mongo

import java.util.{LinkedList, Map}

import org.web3j.utils.Numeric
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.{DateConverter, Etherscan}

object CrossValidation {
  def main(args: Array[String]): Unit = {
    val startBlock = 700500
    val endBlock = 700510
    val apiRateLimit = 200 // ensure max 5 request per second
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

      println("Get blocks' info by using Etherscan API")

      for (ind <- startBlock to endBlock) {
        val block = Etherscan.getBlock(ind.toHexString)

        val timestamp = Numeric.decodeQuantity(block.get("timestamp"))

        if (ind % 100 == 0) {
          println("Current block -> " + ind)
        }

        if (!(block.isEmpty) && block.get("transactions") != "Empty") {
          val txs = block.get("transactions").asInstanceOf[LinkedList[Map[String, String]]]

          txs.forEach((tx: Map[String, String]) => {
            val list = List(
              ("txHash", tx.get("hash")),
              ("date", DateConverter.getDateFromTimestamp(timestamp)),
              ("value", Numeric.decodeQuantity(tx.get("value")).doubleValue() / weiIntoEth.doubleValue()),  // skip 0x
              ("hasContract", "PLACEHOLDER") // just a predefinite value. TODO replace with the real value
            )
            myBlockchain2.append(list)
          })
        }

        // wait for some time - needed to not exceed api rate limit of 5 requests/sec
        Thread.sleep(apiRateLimit) // wait for 1000 millisecond
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
