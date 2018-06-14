package tcs.examples.bitcoin.fuseki

object TestOpReturn {
  def main(args: Array[String]): Unit = {
    val opreturn = new OpReturnGraph(290000l, 490000l)

    opreturn.delete()
    opreturn.opReturnResearch()
  }
}
