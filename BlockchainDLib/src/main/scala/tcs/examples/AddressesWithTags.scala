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

    val txWithTags = new Collection("AddressesWithTags", mongo)
    val tags = new Tag("identities.txt")

    blockchain.foreach(block => {
      block.bitcoinTxs.foreach(tx => {
        tx.outputs.foreach(out => {
          val add: Address = out.getAddress(MainNet.asInstanceOf);
          if(tags.getValue(add) != null) {
            txWithTags.append(List(
              ("txHash", tx.hash),
              ("date", block.date),
              ("value", out.value),
              ("address", add),
              ("tags", tags.getValue(add))
            ))
          }
        })
      })
    })
  }
}
