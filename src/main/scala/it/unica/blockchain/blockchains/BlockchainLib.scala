package it.unica.blockchain.blockchains

import it.unica.blockchain.blockchains.bitcoin.{BitcoinBlockchain, BitcoinSettings}
import it.unica.blockchain.blockchains.ethereum.{EthereumBlockchain, EthereumSettings}
import it.unica.blockchain.blockchains.litecoin.{LitecoinBlockchain, LitecoinSettings}

object BlockchainLib {

  /** Creates a Litecoin blockchain given the Litecoin Core settings.
    *
    * @param settings Litecoin settings (e.g. Litecoin core network, user, password, etc.)
    * @return A Litecoin blockchain instance
    */
  def getLitecoinBlockchain(settings: LitecoinSettings): LitecoinBlockchain = {
    new LitecoinBlockchain(settings)
  }

  /** Creates a Bitcoin blockchain given the Bitcoin Core settings.
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

