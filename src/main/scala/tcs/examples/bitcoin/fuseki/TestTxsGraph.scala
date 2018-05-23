package tcs.examples.bitcoin.fuseki

object TestTxsGraph {
  def main(args: Array[String]): Unit = {
    val graph_tx = new TxsGraph("b5f6e3b217fa7f6d58081b5d2a9a6607eebd889ed2c470191b2a45e0dcb98eb0", 3)

    graph_tx.deleteTxsGraph()
    graph_tx.start(150000l).end(300000l).startTxsGraph(Both)
  }
}
