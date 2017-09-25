package tcs.examples.ethereum

import java.text.SimpleDateFormat
import java.util.Date

import tcs.blockchain.BlockchainLib
import tcs.custom.ethereum.PriceHistorical
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

/**
  * Created by Danieru on 24/09/2017.
  */
object TxWithFees {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain("http://localhost:8545").setStart(70000).setEnd(150000)
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val txWithFees = new Collection("txWithFees", mongo)
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val priceHistorical = PriceHistorical.getPriceHistorical()

    blockchain.foreach(block => {
      if(block.number % 1000 == 0){
        println("Current block ->" + block.number)
      }
      val date = new Date(block.timeStamp.longValue()*1000)
      val dateFormatted = format.format(date)
      block.transactions.foreach(tx => {
        val creates = if(tx.creates == null) "" else tx.creates
        val to = if(tx.to == null) "" else tx.to
        val list = List(
          ("blockHash", block.hash),
          ("txHash", tx.hash),
          ("date", date),
          ("value", tx.value.doubleValue()/weiIntoEth.doubleValue()),
          ("creates", creates),
          ("gas", tx.gas),
          ("fee", (tx.gas * tx.gasPrice)/weiIntoEth),
          ("rate", if(block.timeStamp.longValue() < 1438905600) 0 else priceHistorical.price_usd(dateFormatted))
        )
        txWithFees.append(list)
      })
    })

    txWithFees.close
  }
}
