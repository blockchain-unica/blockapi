package tcs.blockchain.litecoin

import java.net.URL

import com._37coins.bcJsonRpc.{LitecoindClientFactory, LitecoindInterface}
import com.googlecode.jsonrpc4j.HttpException
import org.litecoinj.core._
import org.litecoinj.params.{MainNetParams, TestNet3Params}
import tcs.blockchain.Blockchain
import tcs.utils.ConvertUtils

import scala.collection.mutable

/**
  * Defines a Litecoin blockchain given the Litecoin Core settings.
  *
  * @param settings Litecoin settings (e.g. Litecoin core network, user, password, etc.)
  */

class LitecoinBlockchain(settings: LitecoinSettings) extends Traversable[LitecoinBlock] with Blockchain {

  private var starBlock = 1l
  private var endBlock = 0l
  private var UTXOmap = mutable.HashMap.empty[(Sha256Hash, Long), Long] // Unspent Transaction Output Map

  // Connects to Litecoin Core
  val clientFactory =
    new LitecoindClientFactory(
      new URL(settings.rpcProtocol + "://" + settings.rpcHost + ":" + settings.rpcPort + "/" + settings.rpcPath),
      settings.rpcUser,
      settings.rpcPassword);

  val client: LitecoindInterface = clientFactory.getClient

  // Sets network: either Main network or Test network
  val networkParameters = settings.network match {
    case MainNet => MainNetParams.get
    case TestNet => TestNet3Params.get
  }

  Context.getOrCreate(networkParameters)


  /**
    * Executes the given task for each LitecoinBlock of the blockchain.
    */
  override def foreach[U](f: (LitecoinBlock) => U): Unit = {

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
    * @return LitecoinBlock representation of the block
    */
  override def getBlock(hash: String): LitecoinBlock = {
    val hex = client.getblock(hash, 0)
    val litecoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jBlock = litecoinSerializer.makeBlock(ConvertUtils.hexToBytes(hex))

    LitecoinBlock.factory(jBlock, client.getblock(hash).getHeight, UTXOmap)
  }


  /**
    * Returns a block given its height.
    *
    * @param height Height of the block
    * @return LitecoinBlock representation of the block
    */
  override def getBlock(height: Long): LitecoinBlock = {
    val blockHash = client.getblockhash(height)

    val hex = client.getblock(blockHash, 0)
    val litecoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jBlock = litecoinSerializer.makeBlock(ConvertUtils.hexToBytes(hex))

    LitecoinBlock.factory(jBlock, height)
  }


  /**
    * Calls the factories to build transactions with input values,
    * passing the UTXO map.
    *
    * @param height  height of the block to retrieve
    * @param UTXOmap The Unspent Transaction Output map
    * @return LitecoinBlock representation of the block
    */
  private def getBlock(height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): LitecoinBlock = {
    val blockHash = client.getblockhash(height)

    val hex = client.getblock(blockHash, 0)
    val litecoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jBlock = litecoinSerializer.makeBlock(ConvertUtils.hexToBytes(hex))

    LitecoinBlock.factory(jBlock, height, UTXOmap)
  }

  /**
    * Returns a transaction given its hash
    *
    * @param hash Hash of the transaction
    * @return BitcoinTransaction representation of the transaction
    */

  def getTransaction(hash: String) : LitecoinTransaction= {
    var hex = client.getrawtransaction(hash)
    val litecoinSerializer = new BitcoinSerializer(networkParameters, true)
    val jTx = litecoinSerializer.makeTransaction(ConvertUtils.hexToBytes(hex))

    LitecoinTransaction.factory(jTx)
  }

  /**
    * Sets the first block of the blockchain to visit.
    *
    * @param height Height of the specified block
    * @return This
    */
 override def start(height: Long): LitecoinBlockchain = {
    starBlock = height

    return this
  }

  /**
    * Sets the last block of the blockchain to visit.
    *
    * @param height Height of the specified block
    * @return This
    */
  override def end(height: Long): LitecoinBlockchain = {
    endBlock = height
    return this
  }

}
