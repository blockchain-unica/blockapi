package it.unica.blockchain.analyses.ethereum.mongo

import java.util.{Calendar, TimeZone}

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.{ERC20Transaction, ERC721Transaction}
import it.unica.blockchain.blockchains.ethereum.tokenUtils.TokenType
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

/** This analysis creates a collection of transactions. If the transaction contains a
  * token's method call (ERC20 or ERC721) then is stored into the database.
  * This analysis starts from a specified date and ends after a month.
  *
  * @author Stefano Di Santo
  */

object TokenTransactions {

  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", false, true))
    val mongo = new DatabaseSettings("TokenTransactions")
    val txsCollection = new Collection("transactions", mongo)

    val startDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")) //Coordinated Universal Time
    startDate.set(2016, Calendar.MAY, 24, 0, 0)
    val endDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    endDate.set(2016, Calendar.DECEMBER, 24, 0, 0)

    // Iterating each block
    blockchain.start(startDate).end(endDate).foreach(block => {

      if (block.height % 100 == 0)
        println("Current Block " + block.height + " " + block.date)

      block.txs.foreach(tx => {
        tx match {
          case _: ERC20Transaction =>
            txsCollection.append(
              List(
                ("type", TokenType.ERC20.toString),
                ("methodCalled", tx.asInstanceOf[ERC20Transaction].method),
                ("timestamp", block.date.toString)
              )
            )
          case _: ERC721Transaction =>
            txsCollection.append(
              List(
                ("type", TokenType.ERC721.toString),
                ("methodCalled", tx.asInstanceOf[ERC721Transaction].method),
                ("timestamp", block.date.toString)
              )
            )
          case _ =>
        }
      })
    })
    txsCollection.close
  }
}
