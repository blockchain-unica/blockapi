package tcs.blockchain.bitcoin

/**
  * Framework settings for the Bitcoin blockchain.
  *
  * @param rpcUser             Bitcoin Core user.
  * @param rpcPassword         Bitcoin Core password.
  * @param rpcPort             Bitcoin Core port.
  * @param rpcHost             Bitcoin Core address.
  * @param network             Either Bitcoin Main network or Bitcoin Test network.
  * @param retrieveInputValues True for performing a "deep scan" of the blockchain and retrieve input values.
  */
class BitcoinSettings(
                       val rpcUser: String,
                       val rpcPassword: String,
                       val rpcPort: String,
                       val rpcHost: String,
                       val rpcPath: String = "",
                       val network: Network,
                       val retrieveInputValues: Boolean = false) {

  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           rpcHost: String,
           network: Network) = this(rpcUser, rpcPassword, rpcPort, rpcHost, "", network, false)

  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           rpcHost: String,
           network: Network,
           retrieveInputValues: Boolean) = this(rpcUser, rpcPassword, rpcPort, rpcHost, "", network, retrieveInputValues)


  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           network: Network,
           retrieveInputValues: Boolean) = this(rpcUser, rpcPassword, rpcPort, "localhost", "", network, retrieveInputValues)


  def this(rpcUser: String,
           rpcPassword: String,
           rpcPort: String,
           network: Network) = this(rpcUser, rpcPassword, rpcPort, "localhost", "", network)

}


/**
  * Bitcoin networks: either Main network or Test network.
  */
class Network


/**
  * Bitcoin Main network
  */
object MainNet extends Network


/**
  * Bitcoin Test network
  */
object TestNet extends Network
