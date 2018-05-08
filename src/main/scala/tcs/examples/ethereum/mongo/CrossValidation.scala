package tcs.examples.ethereum.mongo

import java.util

import org.web3j.utils.Numeric
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.{DateConverter, Etherscan}

object CrossValidation {
  def main(args: Array[String]): Unit = {
    val startBlock = 2429000
    val endBlock = 2429129
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
        val response = Option[util.Map[String,Any]](
          Etherscan.getBlock(ind.toHexString)
        ).getOrElse(None)

        if (ind % 100 == 0) {
          println("Current block -> " + ind)
        }

        response match {
          case None => {}
          case block:util.Map[String,Any] => {
            val transactions = block.get("transactions")
            transactions match {
              case "Empty" => {}
              case txs: util.ArrayList[util.Map[String, Any]] => {
                val timestamp = Numeric.decodeQuantity(block.get("timestamp").toString)

                txs.forEach((tx: util.Map[String, Any]) => {
                  val list = List(
                    ("txHash", tx.get("hash")),
                    ("date", DateConverter.getDateFromTimestamp(timestamp)),
                    ("value", Numeric.decodeQuantity(tx.get("value").toString).doubleValue() / weiIntoEth.doubleValue()), // skip 0x
                    ("hasContract", tx.get("hasContract"))
                  )
                  myBlockchain2.append(list)})
              }
            }
          }
        }
      }
      myBlockchain2.close
    } catch {
      case e: Exception => {
        e.printStackTrace();
      }
    }
  }
}
