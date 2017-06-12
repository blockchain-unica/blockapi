package tcs.blockchain.bitcoin

import org.bitcoinj.core.Sha256Hash

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinInput(
                    val txHash: Sha256Hash,
                    val index: Integer,
                    val value: Long,
                    val inScript: BitcoinScript){
}
