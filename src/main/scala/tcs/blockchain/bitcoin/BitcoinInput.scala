package tcs.blockchain.bitcoin

import org.bitcoinj.core.{ECKey, _}
import org.bitcoinj.params.{MainNetParams, TestNet3Params}
import org.bitcoinj.script.ScriptChunk
import tcs.utils.ConvertUtils

import scala.collection.mutable

/**
  * Defines a transaction input of the Bitcoin blockchain.
  *
  * @param redeemedTxHash Hash of the transaction containing the redeemed output.
  * @param value Input value (in Satoshy).
  * @param redeemedOutIndex Index of the output redeemed (w.r.t. the transaction containing the output);
  *                         null if the enclosing transaction is coinbase.
  * @param isCoinbase True if the enclosing transaction is coninbase.
  * @param inScript Input script.
  */
class BitcoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val value: Long,
                    val redeemedOutIndex: Int,
                    val isCoinbase: Boolean,
                    val inScript: BitcoinScript,
                    val sequenceNo: Long,
                    val outPoint: TransactionOutPoint) {


  /**
    * String representation of a BitcoinInput.
    *
    * @return String representation of a BitcoinInput.
    */
  override def toString(): String =
    redeemedTxHash + " " + value + " " + redeemedOutIndex + " " + isCoinbase + " " + inScript


  /**
    * Returns the address that received the funds of this transaction
    * input. Works only for transactions that redeem a P2PKH output
    *
    * @param network Network settings.
    * @return Either the recipient address or None.
    */
  def getAddress(network: Network): Option[Address] = {

    val param = network match {
      case MainNet => MainNetParams.get
      case TestNet => TestNet3Params.get
    }

    try {
      Some(getAddressFromP2PKHInput(inScript.getChunks, param))
    } catch {
      case e: Exception => try {
        Some(getAddressFromP2PSHInput(inScript.getChunks, param))
      } catch {
        case e: Exception => None
      }
    }
  }

  private def getAddressFromP2PKHInput(chuncks: java.util.List[ScriptChunk], param: NetworkParameters) = {

    if (chuncks.size() != 2)
      throw new Exception("Non P2PKH input")

    val keyBytes = chuncks.get(1).data
    val key = ECKey.fromPublicOnly(keyBytes)
    key.toAddress(param)
  }


  private def getAddressFromP2PSHInput(chuncks: java.util.List[ScriptChunk], param: NetworkParameters) = {
    val redeemScriptBytes = chuncks.get(chuncks.size() - 1).data
    Address.fromP2SHHash(param, ConvertUtils.getRIPEMD160Digest(Sha256Hash.hash(redeemScriptBytes)))
  }


  /**
    * Returns the Bitcoin script
    *
    * @return Bitcoin Script of the current input
    */
  def getScript: BitcoinScript = inScript


  /**
    * Returns the sequence no
    *
    * @return Input sequence number
    */
  def getSequenceNo: Long = sequenceNo
}


/**
  * Factories for [[tcs.blockchain.bitcoin.BitcoinInput]] instances.
  */
object BitcoinInput {

  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinInput]] instances.
    * Creates a new input given its BitcoinJ representation.
    * The value will be set to 0.
    *
    * @param input BitcoinJ representation of the input.
    * @return A new BitcoinInput.
    */
  def factory(input: TransactionInput): BitcoinInput = {
    new BitcoinInput(input.getOutpoint.getHash,
      0,
      input.getOutpoint.getIndex.toInt,
      if (input.getConnectedOutput == null) true else false,
      try {
        new BitcoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      },
      input.getSequenceNumber,
      input.getOutpoint
    )
  }


  /**
    * Factory for [[tcs.blockchain.bitcoin.BitcoinInput]] instances.
    * Creates a new input given its BitcoinJ representation.
    * The value will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param input BitcoinJ representation of the input.
    * @param UTXOmap Unspent transaction outputs map.
    * @param blockHeight Height of the block including the enclosing transaction.
    * @param outputs List of outputs of the enclosing transaction.
    * @return A new BitcoinInput.
    */
  def factory(input: TransactionInput, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long, outputs: List[TransactionOutput]): BitcoinInput = {
    val value = UTXOmap.get((input.getOutpoint.getHash, input.getOutpoint.getIndex)) match {
      // The input value corresponds to the connected output: retrieves it from the map
      case Some(l) => {
        UTXOmap.remove((input.getOutpoint.getHash, input.getOutpoint.getIndex))
        l
      }
      // If the map does not contains the value, then the enclosing transaction should be coinbase.
      case None =>
        if (!input.isCoinBase)
          0 // Error case
        else {
          sum(outputs)
        }
    }

    // Create the new BitcoinInput
    new BitcoinInput(input.getOutpoint.getHash,
      value,
      input.getOutpoint.getIndex.toInt,
      if (input.getConnectedOutput == null) true else false,
      try {
        new BitcoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new BitcoinScript(Array())
      },
      input.getSequenceNumber,
      input.getOutpoint
    )
  }

  private def sum(xs: List[TransactionOutput]): Long = {
    xs.map(_.getValue.longValue()).sum
  }
}