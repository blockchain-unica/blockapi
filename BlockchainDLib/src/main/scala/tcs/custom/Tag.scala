package tcs.custom

import org.bitcoinj.core.Address
import scala.collection.mutable
import scala.io.Source

/**
  * Created by Livio on 19/06/2017.
  */
class Tag(val fileName: String){
  var map = mutable.TreeMap.empty[String, String]

  val bufferedSource = Source.fromFile(fileName)
  for (line <- bufferedSource.getLines) {
    var strings : Array[String] = line.toString.split(", ")
    map += (strings(0) -> strings(1))
  }

  bufferedSource.close

  def getValue(address: Address): Option[String] = {
    return map.get(address.toString)
  }
}