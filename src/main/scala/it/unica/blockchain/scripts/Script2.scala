package it.unica.blockchain.scripts

import it.unica.blockchain.externaldata.metadata.Asset

object Script2 {
  def main(args: Array[String]): Unit = {
    Asset.retrieveHoldersAndSaveCounterpartyAssets()
  }
}
