package tcs.blockchain


trait Blockchain {

  // TODO: Add foreach

  def start(height: Long): Blockchain
  def end(height: Long): Blockchain
}