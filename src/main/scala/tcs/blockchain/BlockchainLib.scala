package tcs.blockchain

import tcs.blockchain.bitcoin.{BitcoinBlockchain, BitcoinSettings}
import tcs.blockchain.ethereum.{EthereumBlockchain, EthereumSettings}
import tcs.blockchain.litecoin.{LitecoinBlockchain, LitecoinSettings}

/** Factory for [[tcs.blockchain.bitcoin.BitcoinBlockchain]] istances. */
object BlockchainLib {

  /** Creates a Bitcoin blockchain given the Bitcoin Core settings.
    *
    * @param settings Bitcoin settings (e.g. Bitcoin core network, user, password, etc.)
    * @return A Bitcoin blockchain instance
    */
  def getLitecoinBlockchain(settings: LitecoinSettings): LitecoinBlockchain = {
    new LitecoinBlockchain(settings)
  }

  /** Creates a Litecoin blockchain given the Bitcoin Core settings.
    *
    * @param settings Bitcoin settings (e.g. Bitcoin core network, user, password, etc.)
    * @return A Bitcoin blockchain instance
    */
  def getBitcoinBlockchain(settings: BitcoinSettings): BitcoinBlockchain = {
    new BitcoinBlockchain(settings)
  }

  /**
    * Creates an Ethereum blockchain given the parity web address
    * @param settings Ethereum settings (e.g. url)
    * @return an Ethereum blockchain instance
    */
  def getEthereumBlockchain(settings: EthereumSettings): EthereumBlockchain = {
    new EthereumBlockchain(settings)
  }
}

