package it.unica.blockchain.blockchains.ethereum

import java.io.{BufferedWriter, File, FileWriter}
import java.math.BigInteger
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameterName, DefaultBlockParameterNumber}
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.core.methods.response.{EthBlock, EthGetTransactionReceipt, TransactionReceipt}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject
import scalaj.http.{Http, HttpResponse}
import it.unica.blockchain.pojos.TraceBlockHttpResponse
import it.unica.blockchain.blockchains.Blockchain
import it.unica.blockchain.blockchains.ethereum.tokenUtils.TokenList
import it.unica.blockchain.externaldata.contracts.Etherscan
import org.web3j.protocol.core.Request
import it.unica.blockchain.utils.converter.DateConverter.getDateFromTimestamp
import it.unica.blockchain.utils.converter.DateConverter.getCalendarFromDate
import play.api.libs.json.Json

import scala.collection.JavaConverters._

/**
  * Defines an Ethereum blockchain
  *
  * @param settings Ethereum settings (e.g. url)
  */
class EthereumBlockchain(val settings: EthereumSettings) extends Traversable[EthereumBlock] with Blockchain {

  private var startBlock: Long = 1l
  private var endBlock: Long = 0l

  val web3j = Web3j.build(new HttpService(settings.url)) //Creating Web3J object connected with Parity

  /**
    * Executes the given task for each block in blockchain
    *
    * @param f the task
    * @tparam U type returned
    */
  override def foreach[U](f: EthereumBlock => U): Unit = {

    var height = startBlock
    var endBlock = 0l

    if (this.endBlock == 0l) {
      endBlock = web3j.ethBlockNumber().send().getBlockNumber.longValue()
    } else {
      endBlock = this.endBlock
    }

    while (height <= endBlock) {
      val block = getBlock(height)
      f(block)
      height += 1
    }

    TokenList.updateFiles()
  }


  /**
    * Returns an Ethereum block given its hash
    *
    * @param hash block's hash
    * @return the requested Ethereum block
    */
  override def getBlock(hash: String): EthereumBlock = {
    try {
      getEthereumBlock(
        web3j.ethGetBlockByHash(hash, true).sendAsync().get().getBlock
      )
    } catch {
      case _: Exception => {
        getBlock(hash)
      }
    }
  }


  /**
    * Returns an Ethereum block given its height in blockchain
    *
    * @param height block's height
    * @return the requested Ethereum block
    */
  override def getBlock(height: Long): EthereumBlock = {
    try {
      val block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(height), true).sendAsync().get().getBlock
      getEthereumBlock(block)
    } catch {
      case e: Exception => {
        e.printStackTrace
        throw e
      }
    }
  }


  /**
    * Set the first block in the blockchain to visit
    *
    * @param start block's height
    * @return This
    */
  override def start(start: Long): EthereumBlockchain = {
    this.startBlock = start
    this
  }

  def start(startDate: Calendar): EthereumBlockchain = {
    start(searchBlockByDate(startDate))
  }

  /**
    * Set the last block in the blockchain to visit
    *
    * @param end block's height
    * @return This
    */
  override def end(end: Long): EthereumBlockchain = {
    this.endBlock = end
    this
  }

  def end(endDate: Calendar): EthereumBlockchain = {
    end(searchBlockByDate(endDate))
  }


  private def getEthereumBlock(currBlock: EthBlock.Block): EthereumBlock = {
    val resultBlockTraceJSON = getResultBlockTrace(currBlock.getNumberRaw)
    val transactionReceipts: Map[String, Request[_, EthGetTransactionReceipt]] =
      currBlock.getTransactions.asScala
        .map(_.asInstanceOf[TransactionObject])
        .map((tx) => {
          val txHash: String = tx.get.getHash
          val futureReceipt: Request[_, EthGetTransactionReceipt] = this.web3j.ethGetTransactionReceipt(txHash)
          (txHash, futureReceipt)
        }
        )
        .toMap

    if (resultBlockTraceJSON.isSuccess) {

      val mapper = new ObjectMapper() with ScalaObjectMapper
      mapper.registerModule(DefaultScalaModule)
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      val resultBlockTrace = mapper.readValue[TraceBlockHttpResponse](resultBlockTraceJSON.body)
      var internalTxs: List[EthereumInternalTransaction] = List()

      if (resultBlockTrace.result != null) {
        resultBlockTrace.result.foreach((blockTrace) => {
          blockTrace.getTraceType match {
            case "call" =>
              val value = new BigInteger(blockTrace.getAction.getValue.substring(2), 16)
              if (blockTrace.getTraceAddress.nonEmpty && value.compareTo(new BigInteger("0")) > 0) {
                internalTxs ::= EthereumInternalTransaction(blockTrace.getTransactionHash, blockTrace.getTraceType, EthereumAddress.factory(blockTrace.getAction.getFrom), EthereumAddress.factory(blockTrace.getAction.getTo), value)
              }
            case "suicide" => internalTxs ::= EthereumInternalTransaction(blockTrace.getTransactionHash, blockTrace.getTraceType, EthereumAddress.factory(blockTrace.getAction.getAddress), EthereumAddress.factory(blockTrace.getAction.getRefundAddress), 0)
            case "create" =>
              val value = new BigInteger(blockTrace.getAction.getValue.substring(2), 16)
              if (blockTrace.getTraceAddress.nonEmpty && value.compareTo(new BigInteger("0")) > 0) {
                internalTxs ::= EthereumInternalTransaction(blockTrace.getTransactionHash, blockTrace.getTraceType, EthereumAddress.factory(blockTrace.getAction.getFrom), EthereumAddress.factory(blockTrace.getAction.getTo), value)
              }
            case _ =>
          }
        })
      }

      EthereumBlock.factory(currBlock, internalTxs, transactionReceipts, settings.retrieveVerifiedContracts, settings.searchForTokens, web3j)

    }

    else {
      return EthereumBlock.factory(currBlock, List(), transactionReceipts, settings.retrieveVerifiedContracts, settings.searchForTokens, web3j)
    }
  }


  private def getResultBlockTrace(blockNumber: String): HttpResponse[String] = {
    try {
      Http(settings.url).postData("{\"method\":\"trace_block\",\"params\":[\"" + blockNumber + "\"],\"id\":1,\"jsonrpc\":\"2.0\"}")
        .header("Content-Type", "application/json").asString
    } catch {
      case e: Exception => {
        e.printStackTrace
        throw e
      }
    }
  }


  def getTransactionReceipt(transactionHash: String): Unit = {

    val web3j = Web3j.build(new HttpService(settings.url))

    var receipt: EthGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
    if (receipt.getTransactionReceipt.isPresent) {
      var r: TransactionReceipt = receipt.getTransactionReceipt.get()
      r.getContractAddress
    }
  }

  /** This function search for blocks that have the timestamp equals to
    * the given date.
    *
    * @param date block's date
    * @return the block height
    */
  private def searchBlockByDate(date: Calendar): Long = {
    val timestamp : Timestamp = new Timestamp(date.getTimeInMillis)
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")

    sdf.setTimeZone(date.getTimeZone)
    println("Searching Block for: " + sdf.format(date.getTime) + " ...")

    val page = Http("https://api.etherscan.io/api?module=block&action=getblocknobytime&timestamp="+ timestamp.getTime.toString.substring(0, 10) +"&closest=before").asString.body
    val json = Json.parse(page)
    val result = (json \ "result").get.as[String]

    Thread.sleep(3000) // wait for 3 sec to stay into rate limits

    if(result.matches("[0-9]*")) {
      println("Found Block: " + result)
      result.toLong
    }
    else {
      0l
    }
  }
}
