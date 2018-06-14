package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.converter.DateConverter

object TxWithUTXO {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("utxoAnalysis")

    val txWithUTXO = new Collection("txWithUTXO", mongo)

    val utxoSet = blockchain.getUTXOSetAt(100000)

    blockchain.end(100000).foreach(block => {
      if (block.height % 10000 == 0) println(DateConverter.formatTimestamp(System.currentTimeMillis()) + " - Block: " + block.height)

      block.txs.foreach(tx => {
        tx.outputs.foreach(out => {
          if (utxoSet.contains(tx.hash, out.index))
              txWithUTXO.append(List(
                ("txHash", tx.hash),
                ("date", block.date),
                ("outputIndex", out.index),
                ("outputScript", out.outScript),
                ("scriptType", out.outScript.getScriptType)
              ))
            })
        })
      })
    txWithUTXO.close
  }
}
