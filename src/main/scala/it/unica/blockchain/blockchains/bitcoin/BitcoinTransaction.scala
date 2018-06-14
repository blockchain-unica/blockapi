package it.unica.blockchain.blockchains.bitcoin

import java.text.SimpleDateFormat

import org.bitcoinj.core.{Sha256Hash, Transaction}
import org.bitcoinj.script.Script.ScriptType
import it.unica.blockchain.blockchains.{Transaction => TCSTransaction}
import java.util.Date

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.matching.Regex

/**
  * imported libreries for project number 15 TransactionIp
  */
import net.liftweb.json._
import scala.io.Source._

/**
  * Defines a transaction of the Bitcoin blockchain.
  *
  * @param hash Transaction hash
  * @param txSize Size of the transaction
  * @param inputs List of transaction inputs
  * @param outputs List of transaction outputs
  */
class BitcoinTransaction(
                          override val hash: String,
                          override val date: Date,

                          val txSize: Int,
                          val inputs: List[BitcoinInput],
                          val outputs: List[BitcoinOutput],
                          val lock_time: Long) extends TCSTransaction{

  /**
    * Returns the 'Relayed By' field representing the ip address of the node
    * from which the explorer received the transaction
    * Blockcypher APIs and Token are used (http://www.blockcypher.com)
    * The net.liftweb.json libraries are used to parse the data
    * Documentation (https://www.blockcypher.com/dev/dash/)
    *
    * @return 'relayed by' value (string)
    */
  def getIP(): String = {

    // Almost all resources exist under a given blockchain, and follow this pattern
    val url = "https://api.blockcypher.com/"          // url
    val protocol = "v1/"                              // blockcypher API version
    val coin = "btc/"                                 // coin
    val chain = "main/"                               // chain
    val txs = "txs/"                                  // transactions
    val userToken = "?token=97128b966b9246f28e4dd2bf316065d6"// personal token for free plan (see https://www.blockcypher.com/dev/faq/)

    // bitcoincypher complete url
    val urlComplete: String = url + protocol + coin + chain + txs + hash.mkString + userToken

    // json object
    val jsonFromUrl = fromURL(urlComplete).mkString

    // case class for json blockcypher
    case class root(block_hash: String, block_height: Number, block_index: Number, hash: String, addresses: Array[String], total: Number, fees: Number, size: Number,preference: String,
                    relayed_by: String, confirmed: String, received: String, ver: Number, double_spend: Boolean, vin_sz: Number, vout_sz: Number, confirmations: Number,
                    confidence: Number, inpunts: Array[Object], outputs: Array[Object])

    // val format
    implicit val formats = DefaultFormats

    // json object parsing
    val jValue = parse(jsonFromUrl)

    var jsearch = (jValue \\ "relayed_by").children

    // if 'relayed_by' field is not present return "0" (string)
    if (jsearch.isEmpty == true) {
      return "0"
    }
    else {
      var jsearch = (jValue \ "relayed_by").extract[String]

      /** blockcypher returns a json object composed of url + socket
        * the regex used below are used to have only the ipv4 or ipv6 type (without the socket number) and validate it
        */

      // pattern IPV4 to extract IPV4 from a string
      val patternIPv4 = new Regex("(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])")

      // pattern IPV6 to extract IPV6 from a string
      val patternIPv6 = new Regex("(?<![:.\\w])(?:(?:(?:[A-Fa-f0-9]{1,4}:){6}|(?=(?:[A-Fa-f0-9]{0,4}:){0,6}(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?![:.\\w]))(([0-9A-Fa-f]{1,4}:){0,5}|:)((:[0-9a-fA-F]{1,4}){1,5}:|:)|::(?:[A-Fa-f0-9]{1,4}:){5})(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])|(?:[A-Fa-f0-9]{1,4}:){7}[A-Fa-f0-9]{1,4}|(?=(?:[A-Fa-f0-9]{0,4}:){0,7}[A-Fa-f0-9]{0,4}(?![:.\\w]))(([0-9a-fA-F]{1,4}:){1,7}|:)((:[0-9a-fA-F]{1,4}){1,7}|:)|(?:[A-Fa-f0-9]{1,4}:){7}:|:(:[A-Fa-f0-9]{1,4}){7})(?![.\\w])")

      // search for ipv4 address in json
      val ipv4 = (patternIPv4 findFirstIn jsearch)

      // if ipv4 address does not exist
      if (ipv4 == None) {

        // check if jsearch is an ipv6
        val ipv6 = (patternIPv6 findFirstIn jsearch)

        // if even the ipv6 address does not exist, it returns "0" (string)
        if (ipv6 == None) {
          return "0"
        }

        // returns ipv6
        else {
          return ipv6.mkString
        }
      }
      // returns ipv4
      return ipv4.mkString
    }
  }
  /**
    * Returns the sum of all the input values.
    * If a "deep scan" was not performed, each input value is set to 0.
    *
    * @return Sum of all the input values
    */
  def getInputsSum(): Long = {
    inputs.map(input => input.value).reduce(_ + _)
  }

  /**
    * Returns the sum of all the input values.
    * If TxIndex is not set in your bitcoin client this method will not work
    *
    * @param blockchain instance of the BitcoinBlockchain
    * @return Sum of all the input values
    */
  def getInputsSumUsingTxIndex(blockchain: BitcoinBlockchain): Long = {
    var sum: Long = 0
    for(input <- inputs){
      sum += blockchain.getTransaction(input.getRedeemedTxHashAsString).getOutputValueByIndex(input.getRedeemedOutIndex)
    }
    return sum
  }

  /**
    * Returns the sum of all the output values.
    *
    * @return Sum of all the output values
    */
  def getOutputsSum(): Long = {
    outputs.map(output => output.value).reduce(_ + _)
  }

  /**
    * Returns the list containing all the hashes(as strings) of the input values.
    *
    * @return List of all the hashes(as strings) of the input values
    */
  def getInputsHashList(): List[String] = {
    (inputs.foldLeft(new ListBuffer[String])((list, a) => (list += a.getRedeemedTxHashAsString))).toList
  }


  def getOutputValueByIndex(index: Int): Long = {
    outputs.filter((a) => a.getIndex == index ) match{
      case element :: Nil => element.value
      case _ => 0
    }
  }


  /**
    * Returns the transaction lock time.
    *
    * @return Transaction lock time
    */
  def getLockTime(): Long = {
    lock_time
  }


  /**
    * Returns a string representation of the transaction,
    * including hash, size, list of inputs, and list of outputs.
    *
    * @return String representation of the object.
    */
  override def toString(): String = {
    val stringInputs: String = "[ " + inputs.map(i => i.toString() + " ") + "]"
    val stringOutputs: String = "[ " + outputs.map(o => o.toString() + " ") + "]"

    return hash + " " + txSize + " " + stringInputs + " " + stringOutputs
  }

  def printTransaction(): Unit = {
    val stringInputs: String = "[ " + inputs.map(i => "\n  " + i.toString()) + "\n]"
    val stringOutputs: String = "[ " + outputs.map(o =>"\n  " + o.toString()) + "\n]"
    println()
    println( "Hash: " +  hash)
    println( "TxSize: " + txSize)
    println( "LockTime: " + getLockTime())
    println( "InputsSum: " + getInputsSum())
    println( "OutputsSum: " + getOutputsSum())
    println( "StringInputs: " +  stringInputs)
    println( "StringOutputs: " +  stringOutputs)
    println()
  }

  def getPrintableTransaction(): String = {
    val stringInputs: String = "[ " + inputs.map(i => "\n  " + i.toString()) + "\n]"
    val stringOutputs: String = "[ " + outputs.map(o =>"\n  " + o.toString()) + "\n]"
    "\n" + "Hash: " +  hash + "\nTxSize: " + txSize + "\nLockTime: " + getLockTime() + "\nInputsSum: " + getInputsSum() + "\nOutputsSum: " + getOutputsSum() + "\nStringInputs: " +  stringInputs + "\nStringOutputs: " +  stringOutputs + "\n"
  }


  /**
    * Returns a boolean which states if the transaction is standard or not
    * @return True if the transaction is standard, false otherwise
    */
  def isStandard : Boolean = {
    transactionType match {
      case TxType.TX_STANDARD => hasStandardConditions
      case _ => false
    }
  }


  /**
    * Iterates through the output script list and checks if they are standard or not
    * @return the related type of the enumeration
    */
  def transactionType : TxType.Value = {
    outputs.foreach(out => {
      if(
        out.transOut.getScriptPubKey.getScriptType == ScriptType.P2SH ||
        out.transOut.getScriptPubKey.getScriptType == ScriptType.P2PKH ||
        out.transOut.getScriptPubKey.getScriptType == ScriptType.PUB_KEY ||
        out.transOut.getScriptPubKey.isSentToMultiSig ||
        out.transOut.getScriptPubKey.isOpReturn)
        { return TxType.TX_STANDARD }

      return TxType.TX_NONSTANDARD

    })

    return TxType.TX_NOTYPE
  }


  /**
    * Checks the size of a transaction
    * @return true if the size is less than 100,000 bytes, false otherwise
    */
  private def hasStandardTransactionSize : Boolean = {
    return txSize < 100000
  }


  /**
    * Checks the size of each script
    * @return true if the size is less than 1,650 bytes, false otherwise
    */
  private def hasStandardScriptSize : Boolean =  {
    outputs.foreach(out => {
      val size = out.outScript.getProgram.length
      if(size > 1650){
        return false
      }
    })
    return true
  }


  /**
    * Checks the number of signatures required
    * @return true if the number of signatures is less or equal than 3, false otherwise
    */
  private def hasStandardSignatures : Boolean =  {
    outputs.foreach(out => {
      if(out.transOut.getScriptPubKey.isSentToMultiSig){
        val signatures = out.transOut.getScriptPubKey.getNumberOfSignaturesRequiredToSpend
        if(signatures > 3)  {
          return false
        }
      }
    })
    return true
  }


  /**
    * Checks if each input script pushes only data and not opcodes to the evaluation stack
    * @return true if pushes only data, false otherwise
    */
  private def hasStandardPushData : Boolean =  {
    for(input <- inputs) {
      val chunks = input.inScript.getChunks.asScala
      chunks.foreach(chunk => {
        if (!input.isCoinbase && !chunk.isPushData) {
          return false
        }
      })
    }
    return true
  }


  /**
    * Checks if all conditions are evaluated true
    * @return true if all conditions are respected, false otherwise
    */
  private def hasStandardConditions : Boolean = {
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy")
    if(date.after(format.parse("27-09-2014"))){
      return hasStandardTransactionSize && hasStandardSignatures && hasStandardScriptSize && hasStandardPushData
    }
    return true
  }
}

/**
  * Enumeration of all possible transaction types
  */
object TxType extends Enumeration {
  type TxType = Value
  val TX_NOTYPE,
  TX_STANDARD,
  TX_NONSTANDARD = Value
}

/**
  * Factories for [[it.unica.blockchain.blockchains.bitcoin.BitcoinTransaction]] instances.
  */
object BitcoinTransaction {
  /**
    * Factory for [[it.unica.blockchain.blockchains.bitcoin.BitcoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended BitcoinInput will be set to 0.
    *
    * @param tx BitcoinJ representation of the transaction
    * @return A new BitcoinTransaction
    */

  def factory(tx : Transaction) : BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash.toString, null, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }

  /**
    * Factory for [[it.unica.blockchain.blockchains.bitcoin.BitcoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended BitcoinInput will be set to 0.
    *
    * @param tx BitcoinJ representation of the transaction
    * @param txDate Date in which the containing block has been published
    * @return A new BitcoinTransaction
    */
  def factory(tx : Transaction, txDate : Date): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash.toString, txDate, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }

  /**
    * Factory for [[it.unica.blockchain.blockchains.bitcoin.BitcoinTransaction]] instances.
    * Creates a new transaction given its BitcoinJ representation.
    * Values of each appended BitcoinInput will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param tx BitcoinJ representation of the transaction
    * @param txDate Date in which the containing block has been published
    * @param UTXOmap Unspent transaction outputs map
    * @param blockHeight Height of the enclosing block
    * @return A new BitcoinTransaction
    */
  def factory(tx: Transaction, txDate : Date, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long): BitcoinTransaction = {
    val inputs: List[BitcoinInput] = tx.getInputs.asScala.map(i => BitcoinInput.factory(i, UTXOmap, blockHeight, tx.getOutputs.asScala.toList)).toList
    val outputs: List[BitcoinOutput] = tx.getOutputs.asScala.map(o => BitcoinOutput.factory(o, tx.getHash, UTXOmap)).toList

    // TODO: Test getMessageSize
    return new BitcoinTransaction(tx.getHash.toString, txDate, tx.getMessageSize, inputs, outputs, tx.getLockTime)
  }
}

