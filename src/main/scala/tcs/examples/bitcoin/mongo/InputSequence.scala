package tcs.examples.bitcoin.mongo

import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}

import scala.collection.mutable

/**
  * Created by Livio on 30/08/2017.
  */
object InputSequence {
  def main(args: Array[String]): Unit ={

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))

    var allValues : mutable.HashMap[Long, Int] = new mutable.HashMap[Long, Int]()
    var metadataValues : mutable.HashMap[Long, Int] = new mutable.HashMap[Long, Int]()

    var bkTot : Int = 480000
    var txTot : Long = 0
    var inTot : Long = 0

    blockchain.end(bkTot).foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        txTot += 1
        tx.inputs.foreach(i =>{
          inTot += 1
          allValues.update(i.sequenceNo, allValues.getOrElse(i.sequenceNo, 0) + 1)

          if(tx.getLockTime() == 0){
            metadataValues.update(i.sequenceNo, metadataValues.getOrElse(i.sequenceNo, 0) + 1)
          }
        })
      })
    })

    printf("Blocks: %s, Transactions: %s, Inputs: %s\n", bkTot, txTot, inTot)
    printf("All values\n")
    for ((k,v) <- allValues) printf("SequenceNo: %s, Amount: %s\n", k, v)
    printf("Metadata values\n")
    for ((k,v) <- metadataValues) printf("SequenceNo: %s, Amount: %s\n", k, v)
  }
}
