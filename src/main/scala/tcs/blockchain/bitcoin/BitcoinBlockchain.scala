package tcs.blockchain.bitcoin

import java.net.{InetAddress, URL}

import com._37coins.bcJsonRpc.{BitcoindClientFactory, BitcoindInterface}
import com.googlecode.jsonrpc4j.HttpException
import org.bitcoinj.core.{Context, PeerAddress, PeerGroup, Sha256Hash}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}

import scala.collection.mutable


/**
  * Created by stefano on 12/06/17.
  */
class BitcoinBlockchain(settings: BitcoinSettings) extends Traversable[BitcoinBlock] {

  private var starBlock = 1l
  private var endBlock = 0l

  val clientFactory =
    new BitcoindClientFactory(
      new URL("http://localhost:" + settings.rpcPort + "/"),
      settings.rpcUser,
      settings.rpcPassword);


  val client: BitcoindInterface = clientFactory.getClient

  val networkParameters = settings.network match {
    case MainNet => MainNetParams.get
    case TestNet => TestNet3Params.get
  }
  Context.getOrCreate(networkParameters)
  val peerGroup = new PeerGroup(networkParameters)
  peerGroup.start()

  val addr = new PeerAddress(InetAddress.getLocalHost, networkParameters.getPort)
  peerGroup.addAddress(addr)
  peerGroup.waitForPeers(1).get
  peerGroup.setUseLocalhostPeerWhenPossible(true)

  val peer = peerGroup.getDownloadPeer

  var UTXOmap = mutable.HashMap.empty[(Sha256Hash, Long), Long]

  def getBlock(hash: String) = {
    val future = peer.getBlock(Sha256Hash.wrap(hash))
    val coreBlock = client.getblock(hash)
    val height = coreBlock.getHeight
    BitcoinBlock.factory(future.get, height, UTXOmap)
  }

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

  def getBlock(height: Long) = {
    val blockHash = client.getblockhash(height)
    val future = peer.getBlock(Sha256Hash.wrap(blockHash))
    BitcoinBlock.factory(future.get, height)
  }

  /**
    * This get block call the factories to build transaction with input values,
    * passing the UTXO map.
    *
    * @param height height of the block to retrieve
    * @param UTXOmap
    * @return BitcoinBlock
    */
  private def getBlock(height: Long, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]) = {
    val blockHash = client.getblockhash(height)
    val future = peer.getBlock(Sha256Hash.wrap(blockHash))
    BitcoinBlock.factory(future.get, height, UTXOmap)
  }

  def start(height: Long): BitcoinBlockchain = {
    starBlock = height

    return this
  }

  def end(height: Long): BitcoinBlockchain = {
    endBlock = height
    return this
  }


}
