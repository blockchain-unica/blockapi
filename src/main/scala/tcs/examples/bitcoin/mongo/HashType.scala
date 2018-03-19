package tcs.examples.bitcoin.mongo


import org.bitcoinj.core.Transaction
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.{HashTypeUtils}

/**
  * Created by
  * Chelo Fabrizio
  * Lafhouli Hicham
  * Meloni Antonello
  *
  * This class perform the calculation of the hash type of every signature associated with all inputs
  * of a transaction inside a block in the blockchain
  */

object HashType {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")
    val hashType = new Collection("HashType", mongo)

    blockchain.foreach(block => {

      block.bitcoinTxs.foreach(tx => {

        var inputIndex: Integer = 0

        tx.inputs.foreach(in => {


          if (in.redeemedOutIndex >= 0) {


            val signatures = HashTypeUtils.parse(in.script)


            if (!(signatures == null)) {

              signatures.forEach { s => {

                hashType.append(List(
                  ("txHash", tx.hash),
                  ("index", inputIndex),
                  ("date", block.date),
                  ("hashType", in.getHashType(s))
                ))

              }
              }
            }
            inputIndex += 1
          }

        }
        )
      }
      )
    }
    )
    hashType.close
  }
}
