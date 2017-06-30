package tcs.examples

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}

/**
  * Created by stefano on 12/06/17.
  */
object Main {
  def main(args: Array[String]) = {

    val settings = new BitcoinSettings("tcs", "telecostasmeralda", "8332", MainNet, false)
    val blockchain = BlockchainLib.getBitcoinBlockchain(settings)

    blockchain.foreach(b => println(b.bitcoinTxs(0).outputs(0)))

  }


}
