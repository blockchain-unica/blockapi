package tcs.examples

import tcs.blockchain.BlockchainDlib
import tcs.blockchain.bitcoin.{BitcoinBlockchain, BitcoinSettings, MainNet}
import tcs.mongo.Collection

/**
  * Created by stefano on 13/06/17.
  */
object myBlockchain {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainDlib.getBitcoinBlockchain(new BitcoinSettings("tcs", "telecostasmeralda", "8332", MainNet, false))

    val myBlockchain = new Collection("myBlockchain")

    blockchain.foreach(block => {
      block.bitcoinTxs.foreach(tx => {
          myBlockchain.append(List(
          ("txHash", tx.hash),
          ("blockHash", block.hash),
          ("date", block.date),
          ("inputs", tx.inputs),
          ("outputs", tx.outputs)
        ))
      })
    })

  }
}
