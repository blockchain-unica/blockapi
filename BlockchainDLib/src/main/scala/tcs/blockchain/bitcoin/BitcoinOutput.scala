package tcs.blockchain.bitcoin


import org.bitcoinj.core.{Sha256Hash, TransactionOutput}

import scala.collection.mutable

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinOutput(
                     val index: Int,
                     val value: Long,
                     val outScript: BitcoinScript) {

  override def toString(): String =
    index + " " + value + " " + outScript

  def getMetadata(): String =
    if (!isOpreturn) null else {
      var v1: Integer = outScript.toString.indexOf("[");
      var v2: Integer = outScript.toString.indexOf("]");
      if ((v1 == -1) || (v2 == -1))
        return ""
      else
        return outScript.toString.substring(v1 + 1, v2)
    }

  def isOpreturn(): Boolean = outScript.isOpReturn
}

object BitcoinOutput {
  def factory(output: TransactionOutput): BitcoinOutput = {
    new BitcoinOutput(output.getIndex,
      output.getValue.longValue(),

      try {
        new BitcoinScript(output.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      })
  }


  def factory(output: TransactionOutput, txHash: Sha256Hash, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long]): BitcoinOutput = {

    UTXOmap += ((txHash, output.getIndex.toLong) -> output.getValue.longValue())

    new BitcoinOutput(output.getIndex,
      output.getValue.longValue(),
      try {
        new BitcoinScript(output.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      })
  }
}