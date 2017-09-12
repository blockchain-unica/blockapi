package tcs.examples.mongo

import java.io.PrintWriter
import java.util

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}

/**
  * Created by Livio on 31/08/2017.
  */
object CoinbaseHashes {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    var list : util.ArrayList[String] = new util.ArrayList[String]()

    var i: Int = 1
    blockchain.end(480000).foreach(block => {
      if((i%100)==0)
        println(i)

      list.add(block.bitcoinTxs.head.hash.toString)

      i = i + 1
    })

    new PrintWriter("dictionary.txt"){
      write("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b\n");
      list.forEach(l => {write(l+"\n")}); close
    }
  }
}
