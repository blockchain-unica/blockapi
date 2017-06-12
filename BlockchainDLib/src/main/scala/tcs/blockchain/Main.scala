package tcs.blockchain

import java.net.{InetAddress, URL}
import java.util.concurrent.Future

import com._37coins.bcJsonRpc.{BitcoindClientFactory, BitcoindInterface}
import org.bitcoinj.core._
import org.bitcoinj.params.MainNetParams

/**
  * Created by stefano on 12/06/17.
  */
object Main {
  def main(args: Array[String]) = {
    val clientFactory =
      new BitcoindClientFactory(
        new URL("http://localhost:8332/"),
        "tcs",
        "telecostasmeralda");


    val client: BitcoindInterface = clientFactory.getClient
    val firtBlockHash = client.getblockhash(467251);

    val networkParameters = MainNetParams.get
    Context.getOrCreate(networkParameters)
    val peerGroup = new PeerGroup(networkParameters)
    peerGroup.start()

    val addr = new PeerAddress(InetAddress.getLocalHost, networkParameters.getPort)
    peerGroup.addAddress(addr)
    peerGroup.waitForPeers(1).get
    peerGroup.setUseLocalhostPeerWhenPossible(true)

    peerGroup.getDownloadPeer
    val peer = peerGroup.getDownloadPeer

    val future = peer.getBlock(new Sha256Hash(firtBlockHash))
    var block = future.get

    println(block.getHash)

  }


}
