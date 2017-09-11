package tcs.blockchain

import tcs.blockchain.ethereum.EthereumBlockchain
import tcs.blockchain.bitcoin.{BitcoinBlockchain, BitcoinSettings}

/** Factory for [[tcs.blockchain.bitcoin.BitcoinBlockchain]] istances. */
object BlockchainLib {

  /** Creates a Bitcoin blockchain given the Bitcoin Core settings.
    *
    * @param settings Bitcoin Core settings (e.g. network, user, password, etc.)
    * @return A Bitcoin blockchain instance
    */
  def getBitcoinBlockchain(settings: BitcoinSettings): BitcoinBlockchain = {
    new BitcoinBlockchain(settings)
  }

  /**
    * Creates an Ethereum blockchain given the parity web address
    * @param addr address where parity is listening
    * @return an Ethereum blockchain instance
    */
  def getEthereumBlockchain(addr: String): EthereumBlockchain = {
    new EthereumBlockchain(addr)
  }
}
