package tcs.examples.bitcoin.mongo


import org.bitcoinj.core.Transaction
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import tcs.utils.ParsingScript

/**
  * Realizzare uno script Scala che processa tutti gli input script della blockchain di Bitcoin, classificandoli in base al modificatore di firma utilizzato. Più precisamente, il programma crea un database che, per ogni input script, indica le seguenti informazioni:
    hash della transazione contenente l'input script;
    indice dell'input script;
    data di pubblicazione della transazione (ereditato dal blocco);
    stringa che indica quale modificatore di firma è stato utilizzato.
  */
object  HashType{
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))
    val mongo = new DatabaseSettings("myDatabase")

    val myBlockchain = new Collection("HashType", mongo)
    blockchain.foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        var inputIndex: Integer = 0
        tx.inputs.foreach(in => {

          if (in.redeemedOutIndex >= 0) {
            val signatures = ParsingScript.compute(in.script)
            if (signatures == null) {
            } else {
              signatures.forEach { s => {
                myBlockchain.append(List(
                  ("txHash", tx.hash),
                  ("blockHash", block.hash),
                  ("index", inputIndex) ,
                  ("date", block.date),
                  ("hashType", in.getHashType(s))

                ))
              }
              }
            }

          }
          inputIndex += inputIndex
        }
        )
      }
      )
    }
    )

    myBlockchain.close
  }
}
