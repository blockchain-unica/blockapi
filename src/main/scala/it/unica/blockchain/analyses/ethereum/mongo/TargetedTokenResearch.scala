package it.unica.blockchain.analyses.ethereum.mongo

import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.ethereum.EthereumSettings
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC20Methods.{ERC20Allowance, ERC20Approve, ERC20BalanceOf, ERC20Transfer, ERC20TransferFrom}
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.{ERC20Transaction, ERC721Transaction}
import it.unica.blockchain.blockchains.ethereum.tokenTransactions.ERC721Methods.{ERC721Approve, ERC721BalanceOf, ERC721GetApproved, ERC721IsApprovedForAll, ERC721OwnerOf, ERC721SafeTransferFrom, ERC721SafeTransferFromWithBytes, ERC721SetApprovalForAll, ERC721TransferFrom}
import it.unica.blockchain.blockchains.ethereum.tokenUtils.TargetList
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection

object TargetedTokenResearch {


  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", false, true))
    val mongo = new DatabaseSettings("TokenTarget")
    val txsCollection = new Collection("transactions", mongo)

    val startBlock : Int = 9536496
    val endBlock : Int = 9536496

    TargetList.add("0x6b9f9d8ef588470932b693864a62021cabb65ce9") // BoxKey

    // Iterating each block
    blockchain.start(startBlock.toInt).end(endBlock.toInt).foreach(block => {
      println(block.height)

      block.txs.foreach(tx => {
        tx match {
          case _: ERC20Transaction =>   ERC20Tx(tx.asInstanceOf[ERC20Transaction], txsCollection)
          case _: ERC721Transaction =>  ERC721Tx(tx.asInstanceOf[ERC721Transaction], txsCollection)
          case _ =>
        }
      })
    })
    txsCollection.close
  }

  /** Defines witch method of ERC20 token has been called */
  def ERC20Tx(tx: ERC20Transaction, txsCollection: Collection){

    tx match {
      case _: ERC20Allowance =>
        txsCollection.append(
          List(
            ("type", "ERC20"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC20Allowance].method),
            ("_owner", tx.asInstanceOf[ERC20Allowance].tokenOwner.address),
            ("_spender", tx.asInstanceOf[ERC20Allowance].tokenSpender.address)
          )
        )
      case _: ERC20Approve =>
        txsCollection.append(
          List(
            ("type", "ERC20"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC20Approve].method),
            ("_spender", tx.asInstanceOf[ERC20Approve].tokenSpender.address),
            ("_value", tx.asInstanceOf[ERC20Approve].tokenValue.getValue)
          )
        )
      case _: ERC20BalanceOf =>
        txsCollection.append(
          List(
            ("type", "ERC20"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC20BalanceOf].method),
            ("_owner", tx.asInstanceOf[ERC20BalanceOf].tokenOwner.address)
          )
        )
      case _: ERC20Transfer =>
        txsCollection.append(
          List(
            ("type", "ERC20"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC20Transfer].method),
            ("_to", tx.asInstanceOf[ERC20Transfer].tokenTo.address),
            ("_value", tx.asInstanceOf[ERC20Transfer].tokenValue.getValue)
          )
        )
      case _: ERC20TransferFrom =>
        txsCollection.append(
          List(
            ("type", "ERC20"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC20TransferFrom].method),
            ("_from", tx.asInstanceOf[ERC20TransferFrom].tokenFrom.address),
            ("_to", tx.asInstanceOf[ERC20TransferFrom].tokenTo.address),
            ("_value", tx.asInstanceOf[ERC20TransferFrom].tokenValue.getValue)
          )
        )
    }
  }

  /** Defines witch method of ERC721 token has been called */
  def ERC721Tx(tx: ERC721Transaction, txsCollection: Collection){

    tx match {
      case _: ERC721Approve =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721Approve].method),
            ("_approved", tx.asInstanceOf[ERC721Approve].tokenApproved.address),
            ("_tokenId", tx.asInstanceOf[ERC721Approve].tokenId.getValue)
          )
        )
      case _: ERC721BalanceOf =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721BalanceOf].method),
            ("_owner", tx.asInstanceOf[ERC721BalanceOf].tokenOwner.address)
          )
        )
      case _: ERC721GetApproved =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721GetApproved].method),
            ("_tokenId", tx.asInstanceOf[ERC721GetApproved].tokenId.getValue)
          )
        )
      case _: ERC721IsApprovedForAll =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721IsApprovedForAll].method),
            ("_owner", tx.asInstanceOf[ERC721IsApprovedForAll].tokenOwner.address),
            ("_operator", tx.asInstanceOf[ERC721IsApprovedForAll].tokenOperator.address)
          )
        )
      case _: ERC721OwnerOf =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721OwnerOf].method),
            ("_tokenId", tx.asInstanceOf[ERC721OwnerOf].tokenId.getValue)
          )
        )
      case _: ERC721SafeTransferFrom =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721SafeTransferFrom].method),
            ("_from", tx.asInstanceOf[ERC721SafeTransferFrom].tokenFrom.address),
            ("_to", tx.asInstanceOf[ERC721SafeTransferFrom].tokenTo.address),
            ("_tokenId", tx.asInstanceOf[ERC721SafeTransferFrom].tokenId.getValue)
          )
        )
      case _: ERC721SafeTransferFromWithBytes =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721SafeTransferFromWithBytes].method),
            ("_from", tx.asInstanceOf[ERC721SafeTransferFromWithBytes].tokenFrom.address),
            ("_to", tx.asInstanceOf[ERC721SafeTransferFromWithBytes].tokenTo.address),
            ("_tokenId", tx.asInstanceOf[ERC721SafeTransferFromWithBytes].tokenId.getValue),
            ("_bytes", tx.asInstanceOf[ERC721SafeTransferFromWithBytes].tokenBytes.toString)
          )
        )
      case _: ERC721SetApprovalForAll =>
        txsCollection.append(
          List(
            ("type", "ERC721"),
            ("tx", tx.hash),
            ("methodCalled", tx.asInstanceOf[ERC721SetApprovalForAll].method),
            ("_operator", tx.asInstanceOf[ERC721SetApprovalForAll].tokenOperator.address),
            ("_approved", tx.asInstanceOf[ERC721SetApprovalForAll].tokenApproved)
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
            ("_tokenId", tx.asInstanceOf[ERC721TransferFrom].tokenId.getValue)
          )
        )
    }
  }

}
