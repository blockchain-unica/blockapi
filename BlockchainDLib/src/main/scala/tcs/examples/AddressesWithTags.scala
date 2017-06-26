package tcs.examples

import org.bitcoinj.core.Address
import tcs.blockchain.BlockchainDlib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.custom.Tag
import tcs.mongo.{Collection, MongoSettings}

/**
  * Created by Livio on 19/06/2017.
  */
object AddressesWithTags {
  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainDlib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet, true))
    val mongo = new MongoSettings("myDatabase")

    val outWithTags = new Collection("outWithTags", mongo)
    val tags = new Tag("src\\main\\scala\\tcs\\custom\\input.txt")

    blockchain.foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          val add: Address = out.getAddress(MainNet);
          if(add != null)
            if(tags.getValue(add) != null)
              outWithTags.append(List(
                ("txHash", tx.hash),
                ("date", block.date),
                ("value", out.value),
                ("address", add),
                ("tags", tags.getValue(add))
              ))
        })
      })
    })
  }
}
