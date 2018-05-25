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
    val startBlock:Long = 2010600
    val endBlock:Long =   2010700
    val mongo = new DatabaseSettings("myDatabase")

    getDataFromTool(startBlock, endBlock, mongo)
    getDataFromEtherScan(startBlock, endBlock, mongo)
  }

  def getDataFromTool(startBlock: Long, endBlock: Long, mongo: DatabaseSettings): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val myBlockchain1 = new Collection("ethValidation1", mongo)
    var currentBlockId:Long = startBlock // initialization of current block
    val weiIntoEth = BigInt("1000000000000000000")

    try {
      blockchain.start(startBlock).end(endBlock).foreach(block => {
        currentBlockId = block.height.longValue()

        if (currentBlockId % 1000 == 0) {
          println("Current block -> " + currentBlockId)
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
    } catch {
      case e: Exception => {
        myBlockchain1.close
        println("Errors occurred while processing block " + currentBlockId)
        e.printStackTrace()
        getDataFromTool(currentBlockId+1, endBlock, mongo)
      }
    }
  }

  def getDataFromEtherScan(startBlock: Long, endBlock: Long, mongo: DatabaseSettings): Unit = {
    val myBlockchain2 = new Collection("ethValidation2", mongo)
    val weiIntoEth = BigInt("1000000000000000000")

    println("Get blocks' info by using Etherscan API")

    for (currentBlockId <- startBlock to endBlock) {
      try {
        val response = Option[util.Map[String, Any]](
          Etherscan.getBlock(("0x" + currentBlockId.toHexString))
        ).getOrElse(None)

        if (currentBlockId % 1000 == 0) {
          println("Current block -> " + currentBlockId)
        }

        response match {
          case None => {}
          case block: util.Map[_,_] => {
            val transactions = block.get("transactions")
            transactions match {
              case "Empty" => {}
              case list => {
                val timestamp = Numeric.decodeQuantity(block.get("timestamp").toString)
                val txs = list.asInstanceOf[util.ArrayList[util.Map[String,Any]]]

                txs.forEach(tx => {
                    val list = List(
                      ("txHash", tx.get("hash")),
                      ("date", DateConverter.getDateFromTimestamp(timestamp)),
                      ("value", Numeric.decodeQuantity(tx.get("value").toString).doubleValue() / weiIntoEth.doubleValue()),
                      ("hasContract", tx.get("hasContract"))
                    )
                    myBlockchain2.append(list)
                })
              }
            }
          }
          case _ => {}
        }
      } catch {
        case e: Exception => {
          println("Errors occurred while processing block " + currentBlockId)
          e.printStackTrace()
        }
      }
    }
    myBlockchain2.close
  }
}
