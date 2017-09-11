package tcs.pojos

/**
  * Created by Ferruvich on 03/08/2017.
  */
class BlockTrace(
                  action: Action,
                  blockHash: String,
                  blockNumber: String,
                  result: ResultBlockTrace,
                  error: String,
                  subtraces: Int,
                  traceAddress: List[Int],
                  transactionHash: String,
                  transactionPosition: Int,
                  traceType: String
                ) {

  def this() = {
    this(new Action(), "", "", new ResultBlockTrace(), "", 0, List(), "", 0, "")
  }

  def this(action: Action, blockHash: String, blockNumber: String, result: ResultBlockTrace, subtraces: Int, traceAddress: List[Int], transactionHash: String, transactionPosition: Int, traceType: String) = {
    this(action, blockHash, blockNumber, result, "", subtraces, traceAddress, transactionHash, transactionPosition, traceType)
  }

  def this(action: Action, blockHash: String, blockNumber: String, error: String, subtraces: Int, traceAddress: List[Int], transactionHash: String, transactionPosition: Int, traceType: String) = {
    this(action, blockHash, blockNumber, new ResultBlockTrace(), error, subtraces, traceAddress, transactionHash, transactionPosition, traceType)
  }

  def getAction: Action = {
    this.action
  }

  def getBlockHash: String = {
    this.blockHash
  }

  def getBlockNumber: String = {
    this.blockNumber
  }

  def getResult: ResultBlockTrace = {
    this.result
  }

  def getError: String = {
    this.error
  }

  def getSubtraces: Int = {
    this.subtraces
  }

  def getTraceAddress: List[Int] = {
    this.traceAddress
  }

  def getTransactionHash: String = {
    this.transactionHash
  }

  def getTransactionPosition: Int = {
    this.transactionPosition
  }

  def getTraceType: String = {
    this.traceType
  }

}

