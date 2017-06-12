package tcs.blockchain.bitcoin

import java.util.Date

import org.bitcoinj.core.Sha256Hash

/**
  * Created by stefano on 08/06/17.
  */
class BitcoinBlock(
                    val hash: Sha256Hash,
                    val date: Date,
                    val blockSize: Integer,
                    val bitcoinTxs: List[BitcoinTransaction]){
}
