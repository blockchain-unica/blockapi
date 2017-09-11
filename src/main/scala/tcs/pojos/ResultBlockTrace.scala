package tcs.pojos

/**
  * Created by Ferruvich on 03/08/2017.
  */
class ResultBlockTrace (
                         address: String,
                         code: String,
                         gasUsed: String,
                         output: String
                       ) {

  def this() = {
    this(new String(), new String(), new String(), new String())
  }

  def this(gasUsed: String, output: String) = {
    this(new String(), new String(), gasUsed, output)
  }

  def this(address: String, code: String, gasUsed: String) = {
    this(address, code, gasUsed, new String())
  }

  def getAddress: String = {
    this.address
  }

  def getCode: String = {
    this.code
  }

  def getGasUsed: String = {
    this.gasUsed
  }

  def getOutput: String = {
    this.output
  }
}