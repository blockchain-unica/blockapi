package tcs.examples.bitcoin.fuseki

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.fuseki.{BlockchainURI, GraphModel}
import tcs.db.{DatabaseSettings, Fuseki}


object TestAddresses {
  def main(args: Array[String]): Unit = {

    val addresses = new Addresses("b5f6e3b217fa7f6d58081b5d2a9a6607eebd889ed2c470191b2a45e0dcb98eb0", 3)

    addresses.deleteGraphTx()
    addresses.start(150000l).startAddressGraph(Forward)
  }
}