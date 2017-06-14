package tcs.blockchain.bitcoin


import org.bitcoinj.core.{Sha256Hash, TransactionOutput}

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinOutput(
                     val index: Integer,
                     val value: Long,
                     val outScript: BitcoinScript){

  override def toString(): String =
    index + " " + value + " " + outScript

  def isOpreturn(): Boolean = outScript.isOpReturn

  def getMetadata(): String =
    if(!isOpreturn) null else {
      var v1: Integer = outScript.toString.indexOf("[");
      var v2: Integer = outScript.toString.indexOf("]");
      if((v1 == -1) || (v2 == -1))
        return null;
      else
        return outScript.toString.substring(v1+1, v2)
    }
}

object BitcoinOutput {
  def factory(output: TransactionOutput): BitcoinOutput = {
    new BitcoinOutput(output.getIndex,
                      output.getValue.longValue(),
                      new BitcoinScript(output.getScriptBytes))
  }
}