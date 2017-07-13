package tcs.blockchain

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
}
