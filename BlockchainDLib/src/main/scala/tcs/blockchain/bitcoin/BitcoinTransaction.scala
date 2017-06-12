package tcs.blockchain.bitcoin

import org.bitcoinj.core.Sha256Hash

/**
  * Created by Livio on 12/06/2017.
  */
class BitcoinTransaction(
                          val blockHash: Sha256Hash,
                          val hash: Sha256Hash,
                          val txSize: Integer,
                          val inputs: List[BitcoinInput],
                          val outputs: List[BitcoinOutput]){
}
