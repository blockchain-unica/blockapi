package it.unica.blockchain.analyses.bitcoin.mongo

import java.io._

import com.j256.simplemagic.{ContentInfo, ContentInfoUtil}
import it.unica.blockchain.blockchains.BlockchainLib
import it.unica.blockchain.blockchains.bitcoin.{BitcoinOutput, BitcoinSettings, BitcoinTransaction, MainNet}
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.mongo.Collection
import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.{MediaType, MimeType}
import org.bitcoinj.core.Base58

import scala.collection.mutable.ListBuffer


object UTXOChainFinder {

  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("utxoAnalysis200mila")
    var cont: Int = 0
    var transactionList: ListBuffer[(BitcoinTransaction, BitcoinOutput, ListBuffer[BitcoinOutput])] = ListBuffer()
    var chain: ListBuffer[(BitcoinTransaction, BitcoinOutput, ListBuffer[BitcoinOutput])] = ListBuffer()
    var listUTXOout: ListBuffer[BitcoinOutput] = ListBuffer()
    var spentOutput: BitcoinOutput = null
    var size: Int = 0
    var id: Int = 0
    var outputUnspentList: ListBuffer[Option[Any]] = ListBuffer()
    var hashList: ListBuffer[String] = ListBuffer()
    var i: Int = 0
    var k: Int = 0
    var mimeType: MimeType = null


    val transactionChains = new Collection("transactionChains", mongo)

    val utxoSet = blockchain.getUTXOSetAt(50000)

    blockchain.end(50000).foreach(block => {
      block.txs.foreach(tx => {

        tx.outputs.foreach(out => {
          //if the output is contained into unspent transactions
          if (utxoSet.contains(tx.hash, out.index)) {
            // unspent transactions are counted
            cont += 1
            //the output is saved
            listUTXOout.append(out)
          } else {
            //if the output is spent, is saved
            spentOutput = out;
          }
        })
        // if the number of total transactions minus the number of unspent transactions is equal to 1 (therefore there is a spent output)
        // and cont (number of unspent transactions) is better than 0 so unspent output exists
        if ((tx.outputs.size - cont) == 1 && cont > 0) {
          //transaction data are saved with only one output spent
          transactionList.append((tx, spentOutput, listUTXOout))
          listUTXOout = ListBuffer()
        }
        cont = 0;
      })
    })


    var inPutString: String = null
    var outPutString: String = null
    var indexList: ListBuffer[Int] = ListBuffer()

    // if it contains it we arrived at the last transaction
    while (k < transactionList.size) {
      //at each iteration the index of transactions already visited is taken into account,
      // if already visited they are skipped otherwise they are not
      if (indexList.contains(k)) {
        //if the current index is already present in the list of indexes, the transaction has already been included in another chain then increase k
        k += 1
      } else {

        // outputString is equivalent to the hash of the transaction
        outPutString = transactionList(k)._1.hash;
        //you save the first transaction in the chain if it is the first transaction in the chain
        if (i == 0) {
          chain.append((transactionList(k)._1, transactionList(k)._2, transactionList(k)._3));
        }

        // for each transaction in the transaction list with only one output spent
        transactionList.foreach(pair => {
          //for each input of the transaction
          pair._1.inputs.foreach(in => {
            //save the hash of the transaction that is redeemed by that input
            inPutString = in.redeemedTxHash.toString;
            // compare the hash of the current transaction with the hash of the transaction redeemed for each input
            if (inPutString.equals(outPutString)) {
              //found the input takes the transaction to which that input belongs, the output spent and the list of unspent outputs
              chain.append((pair._1, pair._2, pair._3));
              // the new check will be related to the hash of the new transaction
              outPutString = pair._1.hash;
              //the list of indexes is updated
              indexList.append(i)
            }
          })
          i += 1
        })
        i = 0
        //only chains with at least 5 elements are considered
        if (chain.size >= 5) {
          id += 1
          saveData(chain, id) //saving data in the DB
        }
      }
      chain = ListBuffer() // the chain is canceled
      k += 1
    }


    def saveData(chain: ListBuffer[(BitcoinTransaction, BitcoinOutput, ListBuffer[BitcoinOutput])], id: Int): Unit = {
      var k: Int = 0

      chain.foreach(tuple => {
        // save for each chain the hashes of the transactions belonging to the same
        hashList.append(tuple._1.hash)
        k += 1;
        tuple._3.foreach(output => {
          //for each output of each unspent transaction, take the address (field in which the data is contained in case of chain)
          outputUnspentList.append(output.getAddress(MainNet))
        })
      })

      transactionChains.append(List(
        ("_id", id),
        ("txNumber", k),
        ("hashes", hashList),
        ("Type", fileReader(outputUnspentList)(0)),
        ("TikaType", fileReader(outputUnspentList)(1)),
        ("MimeType", fileReader(outputUnspentList)(2)),
        ("OutputList", outputUnspentList)
      ))
      hashList = ListBuffer()
      outputUnspentList = ListBuffer()
      k = 0
    }


    def fileReader(outPutAddresses: ListBuffer[Option[Any]]): ListBuffer[String] = {
      var buffer: String = ""
      var file: File = null
      var stream: BufferedInputStream = null
      var tikaConfig: TikaConfig = TikaConfig.getDefaultConfig
      var mediaType: MediaType = tikaConfig.getMimeRepository.detect(stream, new Metadata())
      var mimeType = tikaConfig.getMimeRepository.forName(mediaType.toString)
      var writer: FileOutputStream = null
      var tipo, tikaType, mimetype: String = ""
      var typeList: ListBuffer[String] = ListBuffer()
      val typeTika: Tika = new Tika()


      file = new File("./temp")
      file.createNewFile()
      writer = new FileOutputStream(file)

      /*taken every fraction of data present as fake-address it is cleaned (from the keyword Some present from the steps
      * previous, it is decoded in base58 or it is written to a temporary file without extension */
      outPutAddresses.foreach(dataOfOutput => {
        buffer = dataOfOutput.toString.substring(5).replace(")", "")
        writer.write(Base58.decode(buffer))
      })
      writer.close


      /** * Different methods to define the type of the file ***/

      /** * SIMPLE MAGIC ***/
      var util: ContentInfoUtil = new ContentInfoUtil(file.getPath)
      var info: ContentInfo = new ContentInfo("", "", "")
      info = util.findMatch(file)

      if (info == null) {
        tipo = "null"
      } else {
        tipo = info.getMimeType
      }
      typeList.append(tipo)


      /** * TIKA ***/
      tikaType = typeTika.detect(file)
      typeList.append(tikaType)

      /** * TIKA 2ND ***/
      stream = new BufferedInputStream(new FileInputStream(file))
      tikaConfig = TikaConfig.getDefaultConfig
      mediaType = tikaConfig.getMimeRepository.detect(stream, new Metadata())
      mimeType = tikaConfig.getMimeRepository.forName(mediaType.toString)
      mimetype = mimeType.getExtension
      typeList.append(mimetype)

      typeList
    }

    def valueOf(bytes: Array[Byte]) = bytes.map {
      b => String.format("%02X", new java.lang.Integer(b & 0xff))
    }.mkString

    System.out.println("saved")

    transactionChains.close
  }
}