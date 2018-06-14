package it.unica.blockchain.blockchains.bitcoin

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar

import com._37coins.bcJsonRpc.{BitcoindClientFactory, BitcoindInterface}
import com.googlecode.jsonrpc4j.HttpException
import org.bitcoinj.core._
import org.bitcoinj.params.{MainNetParams, TestNet3Params}
import it.unica.blockchain.blockchains.Blockchain
import it.unica.blockchain.utils.converter.ConvertUtils

import scala.collection.mutable
import scala.collection.JavaConversions._


/**
  * Defines a Bitcoin blockchain given the Bitcoin Core settings.
  *
  * @param settings Bitcoin settings (e.g. Bitcoin core network, user, password, etc.)
  */
class BitcoinBlockchain(settings: BitcoinSettings) extends Traversable[BitcoinBlock] with Blockchain {

  private var starBlock : Long = 1l
  private var endBlock : Long = 0l
  private var UTXOmap = mutable.HashMap.empty[(Sha256Hash, Long), Long] // Unspent Transaction Output Map

  // Connects to Bitcoin Core
  val clientFactory =
    new BitcoindClientFactory(
      new URL(settings.rpcProtocol + "://" + settings.rpcHost + ":" + settings.rpcPort + "/" + settings.rpcPath),
      settings.rpcUser,
      settings.rpcPassword);

  val client: BitcoindInterface = clientFactory.getClient

  // Sets network: either Main network or Test network
  val networkParameters = settings.network match {
    case MainNet => MainNetParams.get
    case TestNet => TestNet3Params.get
  }

  Context.getOrCreate(networkParameters)


  /**
    * Executes the given task for each BitcoinBlock of the blockchain.
    */
  override def foreach[U](f: (BitcoinBlock) => U): Unit = {

    var height = starBlock
    var endHeight = 0l

    if (endBlock == 0) {
      val bestBlockHash = client.getbestblockhash()
      val bestBlock = client.getblock(bestBlockHash)
      endHeight = bestBlock.getHeight
    } else {
      endHeight = endBlock
    }

    try {
      while (height <= endHeight) {
        val block = if (settings.retrieveInputValues) getBlock(height, UTXOmap) else getBlock(height)
        f(block)
        height += 1
      }
      println("Done")
    } catch {
      case e: HttpException => println("Error occurred:\n" + e.getMessage)
        e.printStackTrace
    }
  }


  /**
    * Returns a block given its hash.
    *
    * @param hash Hash of the block
    * @return BitcoinBlock representation of the block
    */
  override def getBlock(hash: String): BitcoinBlock = {
    val hex = client.getblock(hash, 0)
    val bitcoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jBlock = bitcoinSerializer.makeBlock(ConvertUtils.hexToBytes(hex))

    BitcoinBlock.factory(jBlock, client.getblock(hash).getHeight, UTXOmap)
  }


  /**
    * Returns a block given its height.
    *
    * @param height Height of the block
    * @return BitcoinBlock representation of the block
    */
  override def getBlock(height: Long): BitcoinBlock = {
    val blockHash = client.getblockhash(height)

    val hex = client.getblock(blockHash, 0)
    val bitcoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jBlock = bitcoinSerializer.makeBlock(ConvertUtils.hexToBytes(hex))

    BitcoinBlock.factory(jBlock, height)
  }


  /**
    * Calls the factories to build transactions with input values,
    * passing the UTXO map.
    *
    * @param height  height of the block to retrieve
    * @param UTXOmap The Unspent Transaction Output map
    * @return BitcoinBlock representation of the block
    */
  private def getBlock(height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): BitcoinBlock = {
    val blockHash = client.getblockhash(height)

    val hex = client.getblock(blockHash, 0)
    val bitcoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jBlock = bitcoinSerializer.makeBlock(ConvertUtils.hexToBytes(hex))

    BitcoinBlock.factory(jBlock, height, UTXOmap)
  }

  /**
    * Returns a transaction given its hash
    *
    * @param hash Hash of the transaction
    * @return BitcoinTransaction representation of the transaction
    */
  def getTransaction(hash: String) : BitcoinTransaction= {
    var hex = client.getrawtransaction(hash, 0)
    val bitcoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jTx = bitcoinSerializer.makeTransaction(ConvertUtils.hexToBytes(hex.toString))

    var json = client.getrawtransaction(hash, 1)
    var map = json.asInstanceOf[java.util.LinkedHashMap[String, Object]]

    var date = new java.util.Date(map.get("blocktime").asInstanceOf[Integer] * 1000l)


    BitcoinTransaction.factory(jTx, date)
  }

  /**
    * Sets the first block of the blockchain to visit.
    *
    * @param height Height of the specified block
    * @return This
    */
  override def start(height: Long): BitcoinBlockchain = {
    starBlock = height

    return this
  }

  def getMemPool(): List[String] ={
    val results = client.getrawmempool()

    results.toList
  }

  /**
    * Sets the last block of the blockchain to visit.
    *
    * @param height Height of the specified block
    * @return This
    */
  override def end(height: Long): BitcoinBlockchain = {
    endBlock = height
    return this
  }


  /**
    * Returns an UTXO set until block with given height.
    *
    * @param blockHeight
    * @return
    */
  def getUTXOSetAt(blockHeight: Long): collection.Set[(String, Long)] = {
    val block = getBlock(blockHeight, UTXOmap)

    this.end(blockHeight).foreach(block => {})

    return UTXOmap.keySet.map(couple => (couple._1.toString, couple._2))
  }
}
