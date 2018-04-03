package tcs.blockchain


trait Blockchain {

  // TODO: Add foreach

  def start(height: Long): Blockchain
  def end(height: Long): Blockchain

  def getBlock(hash: String): Block
  def getBlock(height: Long): Block
}