package tcs.blockchain.ethereum

import java.math.BigInteger

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameterName, DefaultBlockParameterNumber}
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.core.methods.response.EthBlock
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scalaj.http.{Http, HttpResponse}

import tcs.pojos.TraceBlockHttpResponse
import tcs.blockchain.Blockchain


/**
  * Defines an Ethereum blockchain given the parity web address
  *
  * @param url address where parity is listening
  */
class EthereumBlockchain(url: String) extends Traversable[EthereumBlock] with Blockchain {

  private var start: Long = 1l
  private var end: Long = 0l
  private var step: Long = 1l

  //Creating Web3J object connected with Parity
  private val web3j = Web3j.build(new HttpService(url))

  /**
    * Executes the given task for each block in blockchain
    *
    * @param f the task
    * @tparam U type returned
    */
  override def foreach[U](f: EthereumBlock => U): Unit = {

    var height = start
    var endBlock = 0l

    if (this.end == 0l) {
      endBlock = web3j.ethBlockNumber().send().getBlockNumber.longValue()
    } else {
      endBlock = this.end
    }

    while (height <= endBlock) {
      val block = getBlock(height)
      f(block)
      height += step
    }
  }

  /**
    * Returns an Ethereum block given its height in blockchain
    *
    * @param height block's height
    * @return the requested Ethereum block
    */
  def getBlock(height: Long): EthereumBlock = {
    try {
      val block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(height), true).sendAsync().get().getBlock
      getEthereumBlock(block)
    } catch {
      case _: Exception => {
        getBlock(height)
      }
    }
  }

  /**
    * Returns an Ethereum block given its hash
    *
    * @param hash block's hash
    * @return the requested Ethereum block
    */
  def getBlock(hash: String): EthereumBlock = {
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
    * Set the first block in the blockchain to visit
    *
    * @param start block's height
    * @return This
    */
  def setStart(start: Long): EthereumBlockchain = {
    this.start = start
    this
  }

  /**
    * Set the last block in the blockchain to visit
    *
    * @param end block's height
    * @return This
    */
  def setEnd(end: Long): EthereumBlockchain = {
    this.end = end
    this
  }

  /**
    * Set the step visiting blockchain
    *
    * @param step step amount
    * @return This
    */
  def setStep(step: Int): EthereumBlockchain = {
    this.step = step
    this
  }

  def getContractCode(contractAddress: String): String = {
    this.web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send().getCode
  }

  private def getEthereumBlock(currBlock: EthBlock.Block): EthereumBlock = {
    val resultBlockTraceJSON = getResultBlockTrace(currBlock.getNumberRaw)
    if (resultBlockTraceJSON.code.toString.startsWith("40"))
      return EthereumBlock.factory(currBlock, List())
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val resultBlockTrace = mapper.readValue[TraceBlockHttpResponse](resultBlockTraceJSON.body.replaceAll("type", "traceType"))
    var internalTxs: List[EthereumInternalTransaction] = List()
    resultBlockTrace.result.foreach((blockTrace) => {
      blockTrace.getTraceType match {
        case "call" =>
          val value = new BigInteger(blockTrace.getAction.getValue.substring(2), 16)
          if (blockTrace.getTraceAddress.nonEmpty && value.compareTo(new BigInteger("0")) > 0) {
            internalTxs ::= EthereumInternalTransaction(blockTrace.getTransactionHash, blockTrace.getTraceType, blockTrace.getAction.getFrom, blockTrace.getAction.getTo, value)
          }
        case "suicide" => internalTxs ::= EthereumInternalTransaction(blockTrace.getTransactionHash, blockTrace.getTraceType, blockTrace.getAction.getAddress, blockTrace.getAction.getRefundAddress, 0)
        case "create" =>
          val value = new BigInteger(blockTrace.getAction.getValue.substring(2), 16)
          if (blockTrace.getTraceAddress.nonEmpty && value.compareTo(new BigInteger("0")) > 0) {
            internalTxs ::= EthereumInternalTransaction(blockTrace.getTransactionHash, blockTrace.getTraceType, blockTrace.getAction.getFrom, blockTrace.getAction.getTo, value)
          }
        case _ =>
      }
    })
    EthereumBlock.factory(currBlock, internalTxs)
  }

  private def getResultBlockTrace(blockNumber: String): HttpResponse[String] = {
    try {
      Http(this.url).postData("{\"method\":\"trace_block\",\"params\":[\"" + blockNumber + "\"],\"id\":1,\"jsonrpc\":\"2.0\"}")
        .header("Content-Type", "application/json").asString
    } catch {
      case _: Exception => {
        getResultBlockTrace(blockNumber)
      }
    }
  }

}
