package it.unica.blockchain.blockchains.litecoin

/*
import org.litecoinj.core.{ECKey, _}
import org.litecoinj.crypto.TransactionSignature
import org.litecoinj.params.{MainNetParams, TestNet3Params}
import org.litecoinj.script.ScriptChunk
import it.unica.blockchain.utils.converter.ConvertUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Defines a transaction input of the Litecoin blockchain.
  *
  * @param redeemedTxHash Hash of the transaction containing the redeemed output.
  * @param value Input value (in Satoshi).
  * @param redeemedOutIndex Index of the output redeemed (w.r.t. the transaction containing the output);
  *                         null if the enclosing transaction is coinbase.
  * @param isCoinbase True if the enclosing transaction is coinbase.
  * @param inScript Input script.
  */
class LitecoinInput(
                    val redeemedTxHash: Sha256Hash,
                    val value: Long,
                    val redeemedOutIndex: Int,
                    val isCoinbase: Boolean,
                    val inScript: LitecoinScript,
                    val sequenceNo: Long,
                    val outPoint: TransactionOutPoint
                   ){

  /**
    * String representation of a LitecoinInput.
    *
    * @return String representation of a LitecoinInput.
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
    *
    * //@param
    * @return List[SignatureHash] an list of enum value representing the specific hash type.
    */
  def getSignatureHashType(): List[SignatureHash.SignatureHash] ={

    if(inScript==null){
      List[SignatureHash.SignatureHash]()
    }

    else{

      val hashTypelist=new ListBuffer[SignatureHash.SignatureHash]()
      val signatures:List[Array[Byte]]=getSignatures()


      signatures.foreach(sig=>{

        val lastByteSignature:Int = sig.last & 0xff

        val hashType= lastByteSignature match {

          case  0x01  => hashTypelist+=SignatureHash.ALL
          case  0x02  => hashTypelist+=SignatureHash.NONE
          case  0x03  => hashTypelist+=SignatureHash.SINGLE
          case  0x80  => hashTypelist+=SignatureHash.ANYONECANPAY
          case  0x81  => hashTypelist+=SignatureHash.ANYONECANPAY_ALL
          case  0x82  => hashTypelist+=SignatureHash.ANYONECANPAY_NONE
          case  0x83  => hashTypelist+=SignatureHash.ANYONECANPAY_ALL
          case   _    => hashTypelist+=SignatureHash.UNSET

        }
      })

      hashTypelist.toList
    }
  }

  private def getSignatures():List[Array[Byte]]={
    val signature=new ListBuffer[Array[Byte]]()

    inScript.getChunks.forEach(chunk => {
      if(chunk.data != null && TransactionSignature.isEncodingCanonical(chunk.data))
        signature+=chunk.data
    })
    signature.toList

  }

  /**
    * Returns the Litecoin script
    *
    * @return Litecoin script of the current input
    */
  def getScript: LitecoinScript = inScript


  /**
    * Returns the sequence no
    *
    * @return Input sequence number
    */
  def getSequenceNo: Long = sequenceNo

  def getRedeemedTxHashAsString: String = redeemedTxHash.toString
  def getRedeemedOutIndex: Int = redeemedOutIndex
}


/**
  * Factories for [[it.unica.blockchain.blockchains.litecoin.LitecoinInput]] instances.
  */
object LitecoinInput {

  /**
    * Factory for [[it.unica.blockchain.blockchains.litecoin.LitecoinInput]] instances.
    * Creates a new input given its LitecoinJ representation.
    * The value will be set to 0.
    *
    * @param input LitecoinJ representation of the input.
    * @return A new LitecoinInput.
    */
  def factory(input: TransactionInput): LitecoinInput = {
    new LitecoinInput(input.getOutpoint.getHash,
      0,
      input.getOutpoint.getIndex.toInt,
      if (input.getConnectedOutput == null) true else false,
      try {
        new LitecoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new LitecoinScript(Array())
      },
      input.getSequenceNumber,
      input.getOutpoint
    )
  }


  /**
    * Factory for [[it.unica.blockchain.blockchains.litecoin.LitecoinInput]] instances.
    * Creates a new input given its LitecoinJ representation.
    * The value will be set to the correct value
    * by exploiting the UTXO map provided.
    *
    * @param input LitecoinJ representation of the input.
    * @param UTXOmap Unspent transaction outputs map.
    * @param blockHeight Height of the block including the enclosing transaction.
    * @param outputs List of outputs of the enclosing transaction.
    * @return A new LitecoinInput.
    */
  def factory(input: TransactionInput, UTXOmap: mutable.HashMap[(Sha256Hash, Long), Long], blockHeight: Long, outputs: List[TransactionOutput]): LitecoinInput = {
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

    // Create the new LitecoinInput
    new LitecoinInput(input.getOutpoint.getHash,
      value,
      input.getOutpoint.getIndex.toInt,
      if (input.getConnectedOutput == null) true else false,
      try {
        new LitecoinScript(input.getScriptBytes)
      } catch {
        case e: Exception => new LitecoinScript(Array())
      },
      input.getSequenceNumber,
      input.getOutpoint
    )
  }

  private def sum(xs: List[TransactionOutput]): Long = {
    xs.map(_.getValue.longValue()).sum
  }
}
*/