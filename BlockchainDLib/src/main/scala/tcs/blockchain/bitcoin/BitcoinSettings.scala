package tcs.blockchain.bitcoin

/**
  * Created by stefano on 12/06/17.
  */
class BitcoinSettings(
                       val rpcUser: String,
                       val rpcPassword: String,
                       val rpcPort: String,
                       val network: Network,
                       val retrieveInputValues: Boolean = false) {

}

abstract class Network

object MainNet extends Network

object TestNet extends Network
