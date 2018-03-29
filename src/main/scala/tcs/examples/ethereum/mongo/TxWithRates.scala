package tcs.examples.ethereum.mongo

import java.text.SimpleDateFormat
import java.util.Date

import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
import tcs.custom.ethereum.PriceHistorical
import tcs.db.DatabaseSettings
import tcs.mongo.Collection

object TxWithRates {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("myDatabase")
    val weiIntoEth = BigInt("1000000000000000000")
    val txWithRates = new Collection("txWithRates", mongo)
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val priceHistorical = PriceHistorical.getPriceHistorical()

    blockchain.start(70000).end(150000).foreach(block => {
      if(block.number % 1000 == 0){
        println("Current block ->" + block.number)
      }
      val date = new Date(block.timeStamp.longValue()*1000)
      val dateFormatted = format.format(date)
      block.transactions.foreach(tx => {
        val creates = if(tx.addressCreated == null) "" else tx.addressCreated
        val to = if(tx.to == null) "" else tx.to
        val list = List(
          ("txHash", tx.hash),
          ("blockHeight", tx.blockNumber.toString()),
          ("txIndex", tx.transactionIndex),
          ("date", date),
          ("from", tx.from),
          ("to", to),
          ("value", tx.value.doubleValue()/weiIntoEth.doubleValue()),
          ("creates", creates),
          ("rate", if(block.timeStamp.longValue() < 1438905600) 0 else priceHistorical.price_usd(dateFormatted))
        )
        txWithRates.append(list)
      })
    })

    txWithRates.close
  }
}
