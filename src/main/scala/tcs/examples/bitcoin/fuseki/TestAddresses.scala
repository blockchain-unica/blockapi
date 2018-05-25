package tcs.examples.bitcoin.fuseki

object TestAddresses {
  def main(args: Array[String]): Unit = {

    val addresses = new AddressesGraph("b5f6e3b217fa7f6d58081b5d2a9a6607eebd889ed2c470191b2a45e0dcb98eb0", 3)
    addresses.delete()
    addresses.start(150000l).end(160000l).startAddressesGraph(Both)
  }
}