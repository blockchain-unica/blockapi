package tcs.examples.ethereum.mongo

import java.text.SimpleDateFormat
import java.util.Date

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.custom.ethereum.PriceHistorical
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object TxWithFees {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val txWithFees = new Collection("txWithFees", mongo)
    val format = new SimpleDateFormat("yyyy-MM-dd")

    blockchain.start(70000).end(150000).foreach(block => {
      if(block.height % 1000 == 0){
        println("Current block ->" + block.height)
      }
      block.txs.foreach(tx => {
        val creates = if(tx.addressCreated == null) "" else tx.addressCreated
        val to = if(tx.to == null) "" else tx.to
        val list = List(
          ("blockHash", block.hash),
          ("txHash", tx.hash),
          ("date", block.date),
          ("value", tx.value.doubleValue()/weiIntoEth.doubleValue()),
          ("creates", creates),
          ("gas", tx.gas),
          ("fee", (tx.gas * tx.gasPrice)/weiIntoEth),
          ("rate", PriceHistorical.getRate(block.date))
        )
        txWithFees.append(list)
      })
    })

    txWithFees.close
  }
}
