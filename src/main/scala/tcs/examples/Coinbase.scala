package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}

/**
  * Created by Livio on 14/06/2017.
  */
object Coinbase {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))

    var i = 0
    blockchain.end(280964).foreach(block => {

      var script : String = block.bitcoinTxs.head.inputs.head.getScript.toString
      if (script.equals("")) println(i)

      i = i + 1
    })
  }
}