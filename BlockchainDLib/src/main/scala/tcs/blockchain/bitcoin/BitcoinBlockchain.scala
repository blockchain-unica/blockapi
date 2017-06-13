package tcs.blockchain.bitcoin

import java.net.{InetAddress, URL}

import com._37coins.bcJsonRpc.{BitcoindClientFactory, BitcoindInterface}
import com.googlecode.jsonrpc4j.HttpException
import org.bitcoinj.core.{Context, PeerAddress, PeerGroup, Sha256Hash}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}


/**
  * Created by stefano on 12/06/17.
  */
class BitcoinBlockchain(settings: BitcoinSettings) extends Traversable[BitcoinBlock] {

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

  peerGroup.getDownloadPeer
  val peer = peerGroup.getDownloadPeer

  private def getBlock(hash: String) = {
    val future = peer.getBlock(new Sha256Hash(hash))
    val coreBlock = client.getblock(hash)
    val height = coreBlock.getHeight
    BitcoinBlock.factory(future.get, height)
  }

  private def getBlock(height: Int) = {
    val blockHash = client.getblockhash(height)
    val future = peer.getBlock(new Sha256Hash(blockHash))
    BitcoinBlock.factory(future.get, height)
  }

  override def foreach[U](f: (BitcoinBlock) => U): Unit = {

    var height = 1

    try {
      while (true) {
        val block = getBlock(height)
        f(block)
        height += 1
      }
    } catch {
      case e: HttpException => println("Done")
    }


  }


}
