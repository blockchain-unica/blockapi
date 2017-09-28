package tcs.blockchain.bitcoin

import java.net.{InetAddress, URL}

import com._37coins.bcJsonRpc.{BitcoindClientFactory, BitcoindInterface}
import com.googlecode.jsonrpc4j.HttpException
import org.bitcoinj.core.{Context, PeerAddress, PeerGroup, Sha256Hash}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}
import tcs.blockchain.Blockchain

import scala.collection.mutable


/**
  * Defines a Bitcoin blockchain given the Bitcoin Core settings.
  *
  * @param settings Bitcoin Core settings (e.g. network, user, password, etc.)
  */
class BitcoinBlockchain(settings: BitcoinSettings) extends Traversable[BitcoinBlock] with Blockchain {

  private var starBlock = 1l
  private var endBlock = 0l

  // Connects to Bitcoin Core
  val clientFactory =
    new BitcoindClientFactory(
      new URL("http://localhost:" + settings.rpcPort + "/"),
      settings.rpcUser,
      settings.rpcPassword);

  val client: BitcoindInterface = clientFactory.getClient

  // Sets network: either Main network or Test network
  val networkParameters = settings.network match {
    case MainNet => MainNetParams.get
    case TestNet => TestNet3Params.get
  }

  Context.getOrCreate(networkParameters)

  val addr = new PeerAddress(InetAddress.getLocalHost, networkParameters.getPort)

  // Connects to Peer group
  val peerGroup = new PeerGroup(networkParameters)
  peerGroup.start()
  peerGroup.addAddress(addr)
  peerGroup.waitForPeers(1).get
  peerGroup.setUseLocalhostPeerWhenPossible(true)

  val peer = peerGroup.getDownloadPeer

  // Unspent Transaction Output Map
  var UTXOmap = mutable.HashMap.empty[(Sha256Hash, Long), Long]


  /**
    * Executes the given task for each BitcoinBlock of the blockchain.
    */
  override def foreach[U](f: (BitcoinBlock) => U): Unit = {

    var height = starBlock
    var endHeight = 0l

    if(endBlock == 0) {
      val bestBlockHash = client.getbestblockhash()
      val bestBlock = client.getblock(bestBlockHash)
      endHeight =  bestBlock.getHeight
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
    }
  }


  /**
    * Returns a block given its hash.
    *
    * @param hash Hash of the block
    * @return BitcoinBlock representation of the block
    */
  def getBlock(hash: String): BitcoinBlock = {
    val future = peer.getBlock(Sha256Hash.wrap(hash))
    val coreBlock = client.getblock(hash)
    val height = coreBlock.getHeight
    BitcoinBlock.factory(future.get, height, UTXOmap)
  }


  /**
    * Returns a block given its height.
    *
    * @param height Height of the block
    * @return BitcoinBlock representation of the block
    */
  def getBlock(height: Long): BitcoinBlock = {
    val blockHash = client.getblockhash(height)
    val future = peer.getBlock(Sha256Hash.wrap(blockHash))
    BitcoinBlock.factory(future.get, height)
  }


  /**
    * Calls the factories to build transactions with input values,
    * passing the UTXO map.
    *
    * @param height height of the block to retrieve
    * @param UTXOmap The Unspent Transaction Output map
    * @return BitcoinBlock representation of the block
    */
  private def getBlock(height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): BitcoinBlock = {
    val blockHash = client.getblockhash(height)
    val future = peer.getBlock(Sha256Hash.wrap(blockHash))
    BitcoinBlock.factory(future.get, height, UTXOmap)
  }


  /**
    * Sets the first block of the blockchain to visit.
    *
    * @param height Height of the specified block
    * @return This
    */
  def start(height: Long): BitcoinBlockchain = {
    starBlock = height

    return this
  }


  /**
    * Sets the last block of the blockchain to visit.
    *
    * @param height Height of the specified block
    * @return This
    */
  def end(height: Long): BitcoinBlockchain = {
    endBlock = height
    return this
  }


}
