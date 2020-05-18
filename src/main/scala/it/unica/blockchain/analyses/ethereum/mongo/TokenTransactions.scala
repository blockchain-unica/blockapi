package it.unica.blockchain.analyses.ethereum.mongo

import org.apache.commons.lang3.StringUtils

import scala.collection.immutable.HashSet
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.{ERC20BalanceOf, ERC20Transfer, ERC20TransferFrom, ERC721BalanceOf, ERC721OwnerOf, ERC721TransferFrom, EthereumInternalTransaction, EthereumSettings, EthereumTransaction}
import it.unica.blockchain.analyses.ethereum.mongo.levensthein.Helpers._
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._

object TokenTransactions {

  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545"))
    val mongo = new DatabaseSettings("TokenTransactions")
    val txsCollection = new Collection("transactions", mongo)

    // Iterating each block
    blockchain.start(10058360).end(10058460).foreach(block => {

      //if(block.height%100 == 0){
      println("Current Block " + block.height)
      //)}

      block.txs.foreach(tx => {
        tx match {
          case _: ERC20TransferFrom =>
            txsCollection.append(
              List(
                ("type", "ERC20"),
                ("tx", tx.hash),
                ("methodCalled", tx.asInstanceOf[ERC20TransferFrom].method),
                ("_from", tx.asInstanceOf[ERC20TransferFrom].tokenFrom.address),
                ("_to", tx.asInstanceOf[ERC20TransferFrom].tokenTo.address),
                ("_value", tx.asInstanceOf[ERC20TransferFrom].tokenValue.toString)
              )
            )
          case _: ERC20Transfer =>
            txsCollection.append(
              List(
                ("type", "ERC20"),
                ("tx", tx.hash),
                ("methodCalled", tx.asInstanceOf[ERC20TransferFrom].method),
                ("_to", tx.asInstanceOf[ERC20TransferFrom].tokenTo.address),
                ("_value", tx.asInstanceOf[ERC20TransferFrom].tokenValue.toString)
              )
            )
          case _: ERC20BalanceOf =>
            txsCollection.append(
              List(
                ("type", "ERC20"),
                ("tx", tx.hash),
                ("methodCalled", tx.asInstanceOf[ERC20TransferFrom].method),
                ("_owner", tx.asInstanceOf[ERC20TransferFrom].tokenFrom.address)
              )
            )
          case _: ERC721TransferFrom =>
            txsCollection.append(
              List(
                ("type", "ERC721"),
                ("tx", tx.hash),
                ("methodCalled", tx.asInstanceOf[ERC721TransferFrom].method),
                ("_from", tx.asInstanceOf[ERC721TransferFrom].tokenFrom.address),
                ("_to", tx.asInstanceOf[ERC721TransferFrom].tokenTo.address),
                ("_tokenId", tx.asInstanceOf[ERC721TransferFrom].tokenValue.toString)
              )
            )
          case _: ERC721BalanceOf =>
            txsCollection.append(
              List(
                ("type", "ERC721"),
                ("tx", tx.hash),
                ("methodCalled", tx.asInstanceOf[ERC20TransferFrom].method),
                ("_owner", tx.asInstanceOf[ERC20TransferFrom].tokenFrom.address)
              )
            )
          case _: ERC721OwnerOf =>
            txsCollection.append(
              List(
                ("type", "ERC721"),
                ("tx", tx.hash),
                ("methodCalled", tx.asInstanceOf[ERC20TransferFrom].method),
                ("_tokenId", tx.asInstanceOf[ERC20TransferFrom].tokenFrom.address)
              )
            )
          case _ =>
        }
      })
    })
    txsCollection.close
  }
}
