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
<<<<<<< HEAD
  *
  * This class perform the calculation of the hash type of every signature associated with all inputs
  * of a transaction inside a block in the blockchain
=======
>>>>>>> fd567310de429b739598b976d2b9d130b2df12d8
  */

object HashType {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")
    val hashType = new Collection("HashType1", mongo)

    blockchain.foreach(block => {

      block.bitcoinTxs.foreach(tx => {

        var inputIndex: Integer = 0

        tx.inputs.foreach(in => {


          if (in.redeemedOutIndex >= 0) {
<<<<<<< HEAD

            val signatures = HashTypeUtils.parse(in.script)

=======
            
            val signatures = HashTypeUtils.parsing(in.script)
            
>>>>>>> fd567310de429b739598b976d2b9d130b2df12d8
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
<<<<<<< HEAD

=======
>>>>>>> fd567310de429b739598b976d2b9d130b2df12d8
