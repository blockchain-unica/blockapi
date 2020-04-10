package it.unica.blockchain.analyses.bitcoin.fuseki

/**This analysis uses external data.
  * Make sure you have installed all the required libraries!
  * Checkout the README file */

object TestOpReturn {
  def main(args: Array[String]): Unit = {
    val opreturn = new OpReturnGraph(293000l, 490000l)

    opreturn.delete()
    opreturn.opReturnResearch()
  }
}
