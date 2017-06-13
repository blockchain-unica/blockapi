package tcs.blockchain

import java.net.{InetAddress, URL}
import java.util.concurrent.Future

import com._37coins.bcJsonRpc.{BitcoindClientFactory, BitcoindInterface}
import org.bitcoinj.core._
import org.bitcoinj.params.MainNetParams
import tcs.blockchain.bitcoin.{BitcoinScript, BitcoinSettings, MainNet}

/**
  * Created by stefano on 12/06/17.
  */
object Main {
  def main(args: Array[String]) = {

    val settings = new BitcoinSettings("tcs","telecostasmeralda","8332", MainNet, false)
    val blockchain = BlockchainDlib.getBitcoinBlockchain(settings)

    blockchain.foreach(b => println(b.hash))

  }


}
