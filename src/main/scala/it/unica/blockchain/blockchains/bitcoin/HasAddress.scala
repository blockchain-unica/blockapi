package it.unica.blockchain.blockchains.bitcoin

import org.bitcoinj.core.Address

trait HasAddress {
  def getAddress(network: Network): Option[Address]
}
