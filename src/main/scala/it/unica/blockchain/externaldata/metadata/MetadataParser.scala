package it.unica.blockchain.externaldata.metadata

import it.unica.blockchain.utils.converter.{ConvertUtils, RC4}


/**
  * Created by Livio on 14/06/2017.
  */
object MetadataParser {
  def getApplication(key: String, metadata: String): String = {
    if (!metadata.contains(Identifiers.BRACKET)) {
      return Protocols.EMPTY;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.ASCRIBE_CODE1)) {
      return Protocols.ASCRIBE;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.BITALIAS_CODE1)) {
      return Protocols.BITALIAS;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.BITPROOF_CODE1)) {
      return Protocols.BITPROOF;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.BLOCKAI_CODE1)) {
      return Protocols.BLOCKAI;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.BLOCKSIGN_CODE1)) {
      return Protocols.BLOCKSIGN;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.BLOCKSTORE_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.BLOCKSTORE_CODE2) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.BLOCKSTORE_CODE3)) {
      return Protocols.BLOCKSTORE;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.COINSPARK_CODE1)) {
      return Protocols.COINSPARK;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.COLU_CODE1)) {
      return Protocols.COLU;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.COUNTERPARTY_CODE1)) {
      return Protocols.COUNTERPARTY;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.CRYPTOCOPYRIGHT_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.CRYPTOCOPYRIGHT_CODE2)) {
      return Protocols.CRYPTOCOPYRIGHT;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.ETERNITYWALL_CODE1)) {
      return Protocols.ETERNITYWALL;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.FACTOM_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.FACTOM_CODE2) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.FACTOM_CODE3) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.FACTOM_CODE4)) {
      return Protocols.FACTOM;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.HELPERBIT_CODE1)) {
      return Protocols.HELPERBIT;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.LAPREUVE_CODE1)) {
      return Protocols.LAPREUVE;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.MONEGRAPH_CODE1)) {
      return Protocols.MONEGRAPH;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.NICOSIA_CODE1)) {
      return Protocols.NICOSIA;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.NOTARY_CODE1)) {
      return Protocols.NOTARY;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.OMNI_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.OMNI_CODE2)) {
      return Protocols.OMNI;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.OPENASSETS_CODE1)) {
      return Protocols.OPENASSETS;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.OPENCHAIN_CODE1)) {
      return Protocols.OPENCHAIN;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.ORIGINALMY_CODE1)) {
      return Protocols.ORIGINALMY;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.PROOFOFEXISTENCE_CODE1)) {
      return Protocols.PROOFOFEXISTENCE;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.PROVEBIT_CODE1)) {
      return Protocols.PROVEBIT;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.REMEMBR_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.REMEMBR_CODE2)) {
      return Protocols.REMEMBR;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.SMARTBIT_CODE1)) {
      return Protocols.SMARTBIT;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.STAMPD_CODE1)) {
      return Protocols.STAMPD;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.STAMPERY_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.STAMPERY_CODE2) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.STAMPERY_CODE3) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.STAMPERY_CODE4) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.STAMPERY_CODE5) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.STAMPERY_CODE6)) {
      return Protocols.STAMPERY;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.COPYROBO_CODE1) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.COPYROBO_CODE2) ||
      metadata.contains(Identifiers.BRACKET + Identifiers.COPYROBO_CODE3)) {
      return Protocols.COPYROBO;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.PROOFSTACK_CODE1)) {
      return Protocols.PROOFSTACK;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.POET_CODE1)) {
      return Protocols.POET;
    }

    if (metadata.contains(Identifiers.BRACKET + Identifiers.EXONUM_CODE1)) {
      return Protocols.EXONUM;
    }

    val message = extractData(metadata)
    val rc4 = new RC4(ConvertUtils.hexToBytes(key))
    val result : String = ConvertUtils.bytesToHex(rc4.decrypt(ConvertUtils.hexToBytes(message))).toLowerCase

    if (result.startsWith(Identifiers.COUNTERPARTY_CODE1)) {
      return Protocols.COUNTERPARTY
    }

    return Protocols.UNKNOWN;
  }

  def isSegwit(metadata: String): Boolean ={
    if (metadata.contains(Identifiers.SEGWIT_COMMITMENT)) return true
    else return false
  }


  def extractData(message: String): String = {
    var v1: Integer = message.indexOf("[")
    var v2: Integer = message.indexOf("]")
    if ((v1 == -1) || (v2 == -1)) {
      return ""
    }
    else
      return message.substring(v1 + 1, v2)
  }
}
