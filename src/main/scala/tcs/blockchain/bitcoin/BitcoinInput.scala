package tcs.blockchain.bitcoin

import javax.script.ScriptException

import org.bitcoinj.core.{Address, Sha256Hash, TransactionInput, TransactionOutput}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}

import scala.collection.mutable

/**
  * Created by Livio on 12/06/2017.
  */

class BitcoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val value: Long,
                    val redeemedOutIndex: Int,
                    val isCoinbase: Boolean,
                    val inScript: BitcoinScript) {

  override def toString(): String =
    redeemedTxHash + " " + value + " " + redeemedOutIndex + " " + isCoinbase + " " + inScript

  def getAddress(network: Network): Option[Address] = {
    try {
      if (inScript.isPayToScriptHash || inScript.isSentToAddress) {
        network match {
          case MainNet => Some(inScript.getToAddress(MainNetParams.get))
          case TestNet => Some(inScript.getToAddress(TestNet3Params.get))
        }
      } else None
    } catch {
      case _: ScriptException => None
    }
  }


  private def sum(xs: List[TransactionOutput]): Long = {
    xs.map(_.getValue.longValue()).sum
  }

}

object BitcoinInput {
  def factory(input: TransactionInput): BitcoinInput = {
    new BitcoinInput(if (input.getConnectedOutput != null) input.getConnectedOutput.getParentTransactionHash else null,
      0,
      input.getParentTransaction.getInputs.indexOf(input),
      if (input.getConnectedOutput == null) true else false,

      try {
        new BitcoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      }
    )
  }

  def factory(input: TransactionInput, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long, outputs: List[TransactionOutput]): BitcoinInput = {
    val value = UTXOmap.get((input.getOutpoint.getHash, input.getOutpoint.getIndex)) match {
      case Some(l) => {
        UTXOmap.remove((input.getOutpoint.getHash, input.getOutpoint.getIndex))
        l
      }
      case None =>
        if (!input.isCoinBase)
          0 // Error case
        else {
          sum(outputs)
        }
    }


    new BitcoinInput(if (input.getConnectedOutput != null) input.getOutpoint.getHash else null,
      value,
      input.getOutpoint.getIndex.toInt,
      if (input.getConnectedOutput == null) true else false,
      try {
        new BitcoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      }
    )
  }

  private def sum(xs: List[TransactionOutput]): Long = {
    xs.map(_.getValue.longValue()).sum
  }
}