package tcs.examples.bitcoin.mongo

import org.bitcoinj.script.Script.ScriptType
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection


object NonStandardTransactions {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("clustering")

    val nonStandardTransactions = new Collection("nonStandardTransactions", mongo)

    blockchain.start(320000).end(340000).foreach(block => {
      if (block.height % 10000 == 0) println("Block: " + block.height)
      block.txs.foreach(tx => {
        if (!tx.isStandard) {
          nonStandardTransactions.append(List(
            ("h", tx.hash),
            ("d", block.date.getTime),
            ("out", tx.outputs.filter(_.outScript.getScriptType == ScriptType.NO_TYPE)),
            ("size", tx.outputs.filter(_.outScript.getScriptType == ScriptType.NO_TYPE).flatMap(out => Seq(out.outScript.getProgram.length)))
          ))
        }
      })
    })

    nonStandardTransactions.close
  }
}
