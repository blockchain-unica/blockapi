package it.unica.blockchain.blockchains.litecoin

/**
  * Framework settings for the Litecoin blockchain.
  *
  * @param rpcUser             Litecoin Core user.
  * @param rpcPassword         Litecoin Core password.
  * @param rpcProtocol         Litecoin Core protocol.
  * @param rpcPort             Litecoin Core port.
  * @param rpcHost             Litecoin Core address.
  * @param network             Either Litecoin Main network or Litecoin Test network.
  * @param retrieveInputValues True for performing a "deep scan" of the blockchain and retrieve input values.
  */

class LitecoinSettings(
                       val rpcUser: String,
                       val rpcPassword: String,
                       val rpcProtocol : String = "http",
                       val rpcPort: String,
                       val rpcHost: String,
                       val rpcPath: String = "",
                       val network: Network,
                       val retrieveInputValues: Boolean = false) {

  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           rpcHost: String,
           network: Network) = this(rpcUser, rpcPassword, "http", rpcPort, rpcHost, "", network, false)

  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           rpcHost: String,
           network: Network,
           retrieveInputValues: Boolean) = this(rpcUser, rpcPassword, "http", rpcPort, rpcHost, "", network, retrieveInputValues)


  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           network: Network,
           retrieveInputValues: Boolean) = this(rpcUser, rpcPassword, "http", rpcPort, "localhost", "", network, retrieveInputValues)


  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           network: Network) = this(rpcUser, rpcPassword, "http", rpcPort, "localhost", "", network)

}


/**
  * Litecoin networks: either Main network or Test network.
  */
class Network


/**
  * Litecoin Main network
  */
object MainNet extends Network


/**
  * Litecoin Test network
  */
object TestNet extends Network
