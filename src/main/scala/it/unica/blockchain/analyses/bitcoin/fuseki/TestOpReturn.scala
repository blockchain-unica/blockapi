package it.unica.blockchain.analyses.bitcoin.fuseki

object TestOpReturn {
  def main(args: Array[String]): Unit = {
    val opreturn = new OpReturnGraph(293000l, 490000l)

    opreturn.delete()
    opreturn.opReturnResearch()
  }
}
