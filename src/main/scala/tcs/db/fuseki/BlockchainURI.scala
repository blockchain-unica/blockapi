package tcs.db.fuseki

import org.apache.jena.rdf.model.ResourceFactory

object BlockchainURI{

  val properties = "http://blockchain/properties#"

  val BLOCK = "http://blockchain/block/"
  val TX = "http://blockchain/tx/"
  val TX_INFO = "http://blockchain/tx_info/"
  val IN = "http://blockchain/in/"
  val OUT = "http://blockchain/out/"
  val TX_REL = "http://blockchain/tx_rel/"
  val ADDRESS = "http://blockchain/address/"
  val OPRETURN = "http://blockchain/opreturn/"

  //block
  val ISBLOCKOF = ResourceFactory.createProperty(properties + "isBlockOf")
  val HASHBLOCK = ResourceFactory.createProperty(properties + "hashBlock")
  val DATE = ResourceFactory.createProperty(properties + "date")
  val SIZE = ResourceFactory.createProperty(properties + "size")
  val HEIGHT = ResourceFactory.createProperty(properties + "height")

  //tx
  val TXHASH = ResourceFactory.createProperty(properties + "txHash")
  val TXDATE = ResourceFactory.createProperty(properties + "date")
  val TXSIZE = ResourceFactory.createProperty(properties + "txSize")
  val INPUTS = ResourceFactory.createProperty(properties + "inputs")
  val OUTPUTS = ResourceFactory.createProperty(properties + "outputs")
  val LOCKTIME = ResourceFactory.createProperty(properties + "lockTime")
  val TXINPUT = ResourceFactory.createProperty(properties + "txInput")
  val FORWARDTX = ResourceFactory.createProperty(properties + "forwardTx")
  val BACKTX = ResourceFactory.createProperty(properties + "backTx")
  val DEPTH = ResourceFactory.createProperty(properties + "depth")
  val ISOUTOF = ResourceFactory.createProperty(properties + "isOutOf")
  val IN_PROP = ResourceFactory.createProperty(properties + "in")
  val OUT_PROP = ResourceFactory.createProperty(properties + "out")
  val TX_PROP = ResourceFactory.createProperty(properties + "tx_info")
  val ISINOF = ResourceFactory.createProperty(properties + "isInOf")
  val SENTTO = ResourceFactory.createProperty(properties + "sentTo")
  val BACKADDR = ResourceFactory.createProperty(properties + "backAddr")
  val FORWARDADDR = ResourceFactory.createProperty(properties + "forwardAddr")
  val OPRETURN_PROP = ResourceFactory.createProperty(properties + "opReturn")
  val NEXTOPRETURN = ResourceFactory.createProperty(properties + "nextOpReturn")

  //input
  val REDEEMEDTXHASH = ResourceFactory.createProperty(properties + "redeemedTxHash")
  val INPUTVALUE = ResourceFactory.createProperty(properties + "inputValue")
  val REDEEMEDOUTINDEX = ResourceFactory.createProperty(properties + "redeemedOutIndex")
  val ISCOINBASE = ResourceFactory.createProperty(properties + "isCoinbase")
  val INSCRIPT = ResourceFactory.createProperty(properties + "inScript")
  val SEQUENCENO = ResourceFactory.createProperty(properties + "sequenceNo")
  val OUTPOINT = ResourceFactory.createProperty(properties + "outPoint")

  //output
  val INDEX = ResourceFactory.createProperty(properties + "index")
  val VALUE = ResourceFactory.createProperty(properties + "value")
  val TRANSOUT = ResourceFactory.createProperty(properties + "transOut")
  val OUTSCRIPT = ResourceFactory.createProperty(properties + "outScript")
  val OUTADDRESS = ResourceFactory.createProperty(properties + "address")
  val ISOPRETURN = ResourceFactory.createProperty(properties + "isOpReturn")

  //address
  val SENTBY = ResourceFactory.createProperty(properties + "sentBy")
  val RECEIVEDBY = ResourceFactory.createProperty(properties + "receivedBy")
  val ADDRESSPROP = ResourceFactory.createProperty(properties + "addressProp")
  val OUTINFO = ResourceFactory.createProperty(properties + "outInfo")

  //opreturn
  val PROTOCOL = ResourceFactory.createProperty(properties + "protocol")
  val METADATA = ResourceFactory.createProperty(properties + "metadata")

}
