package tcs.examples.bitcoin.fuseki

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.fuseki.{BlockchainURI, GraphModel}
import tcs.db.{DatabaseSettings, Fuseki}


object Prova {
  def main(args: Array[String]): Unit = {

    val fuseki = new DatabaseSettings("prova", Fuseki)

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val modell : GraphModel = new GraphModel(fuseki)

    //modell.deleteDataset()
    var tx = blockchain.getTransaction("b5f6e3b217fa7f6d58081b5d2a9a6607eebd889ed2c470191b2a45e0dcb98eb0")

    var c = 0
    while(c <= 3) {
      println("Creation first graph: " + tx.hash.toString)
      modell.addStatements(BlockchainURI.TX + tx.hash.toString,
        List(
          (BlockchainURI.TXHASH, tx.hash.toString),
          (BlockchainURI.TXSIZE, tx.txSize.toString),
          (BlockchainURI.LOCKTIME, tx.lock_time.toString),
          (BlockchainURI.DEPTH, "0")
        )
      )
      c += 1
      modell.commit()
    }


 }
}