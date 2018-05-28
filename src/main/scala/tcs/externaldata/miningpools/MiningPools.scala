package tcs.externaldata.miningpools

import tcs.blockchain.bitcoin.BitcoinTransaction
import tcs.blockchain.litecoin.LitecoinTransaction

object MiningPools {

  def getBitcoinPool(transaction: BitcoinTransaction): String = {

    if (transaction.inputs.head.isCoinbase) {
      val programByte: Array[Byte] = transaction.inputs.head.inScript.getProgram()
      if (programByte != null) {
        val hex: String = programByte.map("%02x".format(_)).mkString
        if (hex != "") {
          // Known pool codes listed on blockchain.info
          // See https://github.com/blockchain/Blockchain-Known-Pools/blob/master/pools.json for more
          if (hex.contains("416e74506f6f6c3")) return Pools.ANTPOOL
          if (hex.contains("42697446757279")) return Pools.BITFURY
          if (hex.contains("736c757368")) return Pools.SLUSHPOOL
          if (hex.contains("42544343")) return Pools.BTCCPOOL
          if (hex.contains("4254432e434f4d")) return Pools.BTCCOM
          if (hex.contains("566961425443")) return Pools.VIABTC
          if (hex.contains("4254432e544f502")) return Pools.BTCTOP
          if (hex.contains("426974436c7562204e6574776f726b")) return Pools.BITCLUBNETWORK
          if (hex.contains("67626d696e657273")) return Pools.GBMINERS
          if (hex.contains("42697466757279")) return Pools.BITFURY
          if (hex.contains("4269744d696e746572")) return Pools.BITMINTER
          if (hex.contains("4b616e6f")) return Pools.KANOPOOL
          if (hex.contains("426974636f696e2d5275737369612e7275")) return Pools.BITCOINRUSSIA
          if (hex.contains("426974636f696e2d496e646961")) return Pools.BITCOININDIA
          if (hex.contains("425720506f6f6c")) return Pools.BW
          if (hex.contains("3538636f696e2e636f6d")) return Pools._58COIN
          if (hex.contains("706f6f6c2e626974636f696e2e636f6d")) return Pools.BITCOINCOM
          if (hex.contains("436f6e6e656374425443202d20486f6d6520666f72204d696e657273")) return Pools.CONNECTED

          // F2Pool does not have a unique identifier
          if (hex.contains("777868") ||
            hex.contains("66326261636b7570") ||
            hex.contains("68663235") ||
            hex.contains("73796a756e303031") ||
            hex.contains("716c7339") ||
            hex.contains("687578696e6767616f7a68616f")
          ) return Pools.F2POOL
        }
      }
    }

    return Pools.UNKNOWN
  }


  def getEthereumPool(extraData: String): String = {
    if (extraData.contains("7777772e62772e636f6d")) return Pools.BW
    if (extraData.contains("65746865726d696e652d")) return Pools.ETHERMINE
    if (extraData.contains("e4b883e5bda9e7a59ee4bb99e9b1bc")) return Pools.F2POOL2
    if (extraData.contains("4554482e45544846414e532e4f52472d")) return Pools.SPARKPOOL
    if (extraData.contains("6e616e6f706f6f6c2e6f7267")) return Pools.NANOPOOL
    if (extraData.contains("4477617266506f6f6c")) return Pools.DWARF
    if (extraData.contains("7869786978697869")) return Pools.BITCLUBPOOL
    if (extraData.contains("657468706f6f6c2d")) return Pools.ETHPOOL2
    if (extraData.contains("70616e64615f706f6f6c")) return Pools.PANDAPOOL
    if (extraData.contains("786e706f6f6c2e636e")) return Pools.XNPOOL
    if (extraData.contains("7575706f6f6c2e636e")) return Pools.UUPOOL
    if (extraData.contains("7761746572686f6c652e696f")) return Pools.WATERHOLE
    if (extraData.contains("5553492054656368")) return Pools.USI
    if (extraData.contains("7777772e627463632e72656e")) return Pools.BTCC
    if (extraData.contains("457468657265756d50504c4e532f326d696e657273")) return Pools.PPLNS
    if (extraData.contains("4f70656e457468657265756d506f6f6c2f74776574682e7477")) return Pools.TWETH
    if (extraData.contains("d5830107068650617269747986312e32302e30826c69")) return Pools.COINOTRON2

    return Pools.UNKNOWN
  }

  //TODO: mining pools by hex per litecoin (verificarli)
  //https://bitmakler.net/mining_Litecoin-LTC__pools

  def getLitecoinPool(transaction: LitecoinTransaction): String = {

    if (transaction.inputs.head.isCoinbase) {
      val programByte: Array[Byte] = transaction.inputs.head.inScript.getProgram()
      if (programByte != null) {
        val hex: String = programByte.map("%02x".format(_)).mkString
        if (hex != "") {
          /** Antpool, ViaBTC and F2Pool mine LTC too.
            * Currently searching for others LTC pools hex sign
            * Will add them soon.
            * LTCTOP and LitecoinPool found by coinbase hex
            */
        }
        if (hex.contains("4c54432e544f50")) return Pools.LTCTOP
        if (hex.contains("566961425443")) return Pools.VIABTC
        if (hex.contains("2f4c502f")) return Pools.LITECOINPOOL
        if (hex.contains("416e74506f6f6c3")) return Pools.ANTPOOL

        // F2Pool does not have a unique identifier
        if (hex.contains("777868") ||
          hex.contains("66326261636b7570") ||
          hex.contains("68663235") ||
          hex.contains("73796a756e303031") ||
          hex.contains("716c7339") ||
          hex.contains("687578696e6767616f7a68616f")
        ) return Pools.F2POOL
      }
    }
    return Pools.UNKNOWN
  }

}

