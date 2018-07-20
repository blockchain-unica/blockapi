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



object UTXOChainFinder{

  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new DatabaseSettings("utxoAnalysis200mila")
    var cont: Int = 0
    var transactionList : ListBuffer[(BitcoinTransaction, BitcoinOutput, ListBuffer[BitcoinOutput])] = ListBuffer()
    var chain : ListBuffer[(BitcoinTransaction, BitcoinOutput, ListBuffer[BitcoinOutput])] = ListBuffer()
    var listUTXOout: ListBuffer[BitcoinOutput] = ListBuffer()
    var spentOutput : BitcoinOutput = null
    var size : Int = 0
    var id : Int = 0
    var outputUnspentList : ListBuffer[Option[Any]] = ListBuffer()
    var hashList : ListBuffer[String] = ListBuffer()
    var i : Int = 0
    var k : Int = 0
    var mimeType : MimeType = null




    val transactionChains = new Collection("transactionChains", mongo)

    val utxoSet = blockchain.getUTXOSetAt(50000)

    blockchain.end(50000).foreach(block => {
      block.txs.foreach(tx => {
        //per ogni transazione di ogni blocco si prende l'output
        tx.outputs.foreach(out => {
         //se l'output e` contenuto nelle set di transazioni unspent
          if (utxoSet.contains(tx.hash, out.index)){
            // si contano le transazioni unspent
            cont+=1
            //si salva l'output nella lista di output
            listUTXOout.append(out)
          }else{
              //se trova un output speso si salva
            spentOutput=out;
          }
        })
        // se il numero di transazioni totali meno il numero di transazioni unspent e' uguale a 1 (quindi esiste un output speso)
        // e cont (numero di transazioni unspent) e' meggiore di 0 quindi esistono output unspent
        if((tx.outputs.size-cont)==1 && cont > 0){
          //si salvano i dati delle transazioni con un solo output speso
          transactionList.append((tx, spentOutput,listUTXOout))
          listUTXOout = ListBuffer()
        }
        cont=0;
      })
    })



    var inPutString: String = null
    var outPutString: String = null
    var indexList: ListBuffer[Int] = ListBuffer()

    // se la contiene siamo arrivati all'ultima transazione
    while(k<transactionList.size) {
      //ad ogni iterazione si tiene conto dell'indice delle transazioni gia' visitate, se gia' visitate vengono saltate in caso contrario no
        if (indexList.contains(k)){
            //se nella lista di indici e' gia' presente l'indice corrente, la transazione e' stata gia' inclusa in un'altra catena quindi si incrementa k
            k += 1
        }else {

          // outputString equivale all'hash della transazione
          outPutString = transactionList(k)._1.hash;
          //si salva la prima transazione della catena se si tratta della prima transazione della catena
          if(i == 0){
            chain.append((transactionList(k)._1, transactionList(k)._2, transactionList(k)._3));
          }

          // per ogni transazione nella lista delle transazioni con un solo output speso
          transactionList.foreach(pair => {
            //per ogni input della transazione
            pair._1.inputs.foreach(in => {
              //si salva l'hash della transazione di cui quell'input fa il redeem
              inPutString = in.redeemedTxHash.toString;
              // si confronta l'hash della transazione attuale con l'hash della transazione di cui ogni input ha fatto il redeem
              if (inPutString.equals(outPutString)) {
                //trovato l'input si prende la transazione a cui quell'input appartiene, l'output speso e la lista di output non spesi
                chain.append((pair._1, pair._2, pair._3));
                // il nuovo controllo sara' relativo all'hash della nuova transazione
                outPutString = pair._1.hash;
                //si aggiorna la lista degli indici
                indexList.append(i)
              }
            })
            i += 1
          })
          i = 0
          //si prendono in considerazione solo le catene con almeno 5 elementi
          if(chain.size >= 5){
            id+=1
            saveData(chain, id) //salvataggio dei dati nel DB
          }
        }
      chain = ListBuffer() // si cancella la catena
      k += 1
    }



    def saveData(chain : ListBuffer[(BitcoinTransaction, BitcoinOutput, ListBuffer[BitcoinOutput])], id:Int): Unit = {
      var k : Int = 0

      //si scorre la catena
      chain.foreach(tuple => {
        // salva per ogni catena gli hash delle transazioni appartenenti alla stessa
        hashList.append(tuple._1.hash)
        k += 1;
        tuple._3.foreach(output => {
            //per ogni output di ogni transazione non speso, si prende l'indirizzo (campo in cui sono contenuti i dati in caso di catena)
          outputUnspentList.append(output.getAddress(MainNet))
        })
      })

        // scrittura dei dati nel DB
        transactionChains.append(List(
          ("_id", id),
          ("txNumber", k),
          ("hashes", hashList),
          ("Type", fileReader(outputUnspentList)(0)),
          ("TikaType", fileReader(outputUnspentList)(1)),
          ("MimeType", fileReader(outputUnspentList)(2)),
          ("OutputList", outputUnspentList)
        ))
      //pulizia delle liste
      hashList = ListBuffer()
      outputUnspentList = ListBuffer()
      k = 0
    }


    def fileReader(outPutAddresses :ListBuffer[Option[Any]]) : ListBuffer[String] ={
      var buffer : String = ""
      var file : File = null
      var stream : BufferedInputStream = null
      var tikaConfig : TikaConfig = TikaConfig.getDefaultConfig
      var mediaType : MediaType = tikaConfig.getMimeRepository.detect(stream, new Metadata())
      var mimeType = tikaConfig.getMimeRepository.forName(mediaType.toString)
      var writer: FileOutputStream = null
      var tipo, tikaType, mimetype : String = ""
      var typeList : ListBuffer[String] = ListBuffer()
      val typeTika: Tika = new Tika()


      file = new File("./temp")
      file.createNewFile()
      writer= new FileOutputStream(file)

      /*presa ogni frazione di dato presente come fake-address la si ripulisce (dalla keyword Some presente dai passi
      * precedenti, si decodifica in base58 o li scrive su un file temporaneo privo di estensione*/
      outPutAddresses.foreach(dataOfOutput =>{
          buffer = dataOfOutput.toString.substring(5).replace(")","")
          writer.write(Base58.decode(buffer))
      })
      writer.close


      /*** Diversi metodi per definire il tipo del file***/

      /*** SIMPLE MAGIC***/
      var util : ContentInfoUtil = new ContentInfoUtil(file.getPath)
      var info : ContentInfo = new ContentInfo("","","")
      info = util.findMatch(file)

      if(info == null){
        tipo = "null"
      }else{
        tipo = info.getMimeType
      }
      typeList.append(tipo)


      /*** TIKA ***/
      tikaType = typeTika.detect(file)
      typeList.append(tikaType)

      /*** TIKA 2ND***/
      stream= new BufferedInputStream(new FileInputStream(file))
      tikaConfig = TikaConfig.getDefaultConfig
      mediaType = tikaConfig.getMimeRepository.detect(stream, new Metadata())
      mimeType = tikaConfig.getMimeRepository.forName(mediaType.toString)
      mimetype = mimeType.getExtension
      typeList.append(mimetype)

      typeList
    }

    def valueOf(bytes : Array[Byte]) = bytes.map{
      b => String.format("%02X", new java.lang.Integer(b & 0xff))
    }.mkString
        System.out.println("saved")

    transactionChains.close
  }
}