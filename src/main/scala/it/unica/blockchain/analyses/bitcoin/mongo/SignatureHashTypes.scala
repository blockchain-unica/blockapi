package it.unica.blockchain.analyses.bitcoin.mongo


import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinSettings, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection


/**
  * Created by
  * Chelo Fabrizio
  * Lafhouli Hicham
  * Meloni Antonello
  *
  * This class perform the calculation of the hash type of every signature associated with all inputs
  * of a transaction inside a block in the blockchain
  */

object SignatureHashTypes {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")
    val signatureHashTypes = new Collection("signatureHashTypes", mongo)

      blockchain.end(100000).foreach(block => {

        if(block.height % 1000 == 0){
          println(block.height)
        }


        block.txs.foreach(tx => {
          var inputIndex: Integer = 0

          tx.inputs.foreach(in => {
            if (in.redeemedOutIndex >= 0) {
              in.getSignatureHashType().foreach(s => {
                signatureHashTypes.append(List(
                  ("txHash", tx.hash),
                  ("index", inputIndex),
                  ("date", block.date),
                  ("hashType", s)
                ))

              })
              inputIndex += 1
            }
          })
        })
      })

      signatureHashTypes.close
    }
}