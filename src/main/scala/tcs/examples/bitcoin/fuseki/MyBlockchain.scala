package tcs.examples.bitcoin.fuseki

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.fuseki.{BlockchainURI, GraphModel}
import tcs.db.{DatabaseSettings, Fuseki}


object MyBlockchain {
  def main(args: Array[String]): Unit = {

    val fuseki = new DatabaseSettings("myBlockchain", Fuseki)

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val modell : GraphModel = new GraphModel(fuseki)
    blockchain.start(1).end(276089).foreach(block => {

      modell.addStatements(BlockchainURI.BLOCK + block.height.toString,
        List(
          (BlockchainURI.SIZE, block.size.toString()),
          (BlockchainURI.HEIGHT, block.height.toString())
        )
      )

      block.txs.foreach(tx => {
        modell.addStatements(
          BlockchainURI.TX + tx.hash.toString,
          List(
            (BlockchainURI.TXHASH, tx.hash.toString),
            (BlockchainURI.TXSIZE, tx.txSize.toString),
            (BlockchainURI.DATE, tx.date.toString),
            (BlockchainURI.LOCKTIME, tx.lock_time.toString)
          ),
          (BlockchainURI.BLOCK + block.height.toString, BlockchainURI.ISBLOCKOF)
        )

        tx.inputs.foreach(in => {
          modell.addStatements(
            BlockchainURI.IN + in.redeemedTxHash.toString + "/" + in.sequenceNo.toString,
            List(
              (BlockchainURI.REDEEMEDTXHASH, in.redeemedTxHash.toString),
              (BlockchainURI.INPUTVALUE, in.value.toString),
              (BlockchainURI.REDEEMEDOUTINDEX, in.redeemedOutIndex.toString),
              (BlockchainURI.ISCOINBASE, in.isCoinbase.toString),
              (BlockchainURI.SEQUENCENO, in.sequenceNo.toString),
              (BlockchainURI.OUTPOINT, in.outPoint.toString),
              (BlockchainURI.INSCRIPT, in.inScript.toString)
            ),
            (BlockchainURI.TX + tx.hash.toString, BlockchainURI.INPUTS)
          )
        })

        tx.outputs.foreach(out => {
          modell.addStatements(
            BlockchainURI.OUT + tx.hash.toString + "/" + out.index.toString,
            List(
              (BlockchainURI.INDEX, out.index.toString),
              (BlockchainURI.OUTSCRIPT, out.outScript.toString),
              (BlockchainURI.VALUE, out.value.toString),
              (BlockchainURI.TRANSOUT, out.transOut.toString)
            ),
            (BlockchainURI.TX + tx.hash.toString, BlockchainURI.OUTPUTS)
          )
        })
      })

      if (block.height % 1 == 0) {
        println(block.height)
      }
    })
    modell.commit()

  }
}