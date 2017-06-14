package tcs.custom

/**
  * Created by Livio on 14/06/2017.
  */
object OpReturn {
  private var protocol: String = null

  def getProtocol(metadata: String): String = {
      if(!metadata.contains("[")){
        this.protocol = Protocol.EMPTY;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.ASCRIBE_CODE1)){
        this.protocol = Protocol.ASCRIBE;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.BITPROOF_CODE1)){
        this.protocol = Protocol.BITPROOF;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.BLOCKAI_CODE1)){
        this.protocol = Protocol.BLOCKAI;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.BLOCKSIGN_CODE1)){
        this.protocol = Protocol.BLOCKSIGN;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.BLOCKSTORE_CODE1) ||
        metadata.contains(Codes.BRACKET + Codes.BLOCKSTORE_CODE2) ||
        metadata.contains(Codes.BRACKET + Codes.BLOCKSTORE_CODE3)){
        this.protocol = Protocol.BLOCKSTORE;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.COINSPARK_CODE1)){
        this.protocol = Protocol.COINSPARK;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.COLU_CODE1)){
        this.protocol = Protocol.COLU;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.CRYPTOCOPYRIGHT_CODE1) ||
        metadata.contains(Codes.BRACKET + Codes.CRYPTOCOPYRIGHT_CODE2)){
        this.protocol = Protocol.CRYPTOCOPYRIGHT;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.ETERNITYWALL_CODE1)){
        this.protocol = Protocol.ETERNITYWALL;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.FACTOM_CODE1) ||
        metadata.contains(Codes.BRACKET + Codes.FACTOM_CODE2) ||
        metadata.contains(Codes.BRACKET + Codes.FACTOM_CODE3) ||
        metadata.contains(Codes.BRACKET + Codes.FACTOM_CODE4)){
        this.protocol = Protocol.FACTOM;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.LAPREUVE_CODE1)){
        this.protocol = Protocol.LAPREUVE;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.MONEGRAPH_CODE1)){
        this.protocol = Protocol.MONEGRAPH;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.NICOSIA_CODE1)){
        this.protocol = Protocol.NICOSIA;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.OMNI_CODE1)){
        this.protocol = Protocol.OMNI;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.OPENASSETS_CODE1)){
        this.protocol = Protocol.OPENASSETS;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.ORIGINALMY_CODE1)){
        this.protocol = Protocol.ORIGINALMY;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.PROOFOFEXISTENCE_CODE1)){
        this.protocol = Protocol.PROOFOFEXISTENCE;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.PROVEBIT_CODE1)){
        this.protocol = Protocol.PROVEBIT;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.REMEMBR_CODE1) ||
        metadata.contains(Codes.BRACKET + Codes.REMEMBR_CODE2)){
        this.protocol = Protocol.REMEMBR;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.SMARTBIT_CODE1)){
        this.protocol = Protocol.SMARTBIT;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.STAMPD_CODE1)){
        this.protocol = Protocol.STAMPD;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.STAMPERY_CODE1) ||
        metadata.contains(Codes.BRACKET + Codes.STAMPERY_CODE2) ||
        metadata.contains(Codes.BRACKET + Codes.STAMPERY_CODE3) ||
        metadata.contains(Codes.BRACKET + Codes.STAMPERY_CODE4) ||
        metadata.contains(Codes.BRACKET + Codes.STAMPERY_CODE5)){
        this.protocol = Protocol.STAMPERY;
        return metadata;
      }

      if(metadata.contains(Codes.BRACKET + Codes.TRADLE_CODE1)){
        this.protocol = Protocol.TRADLE;
        return metadata;
      }

    this.protocol = Protocol.UNKNOWN;
    return metadata;
  }
}
