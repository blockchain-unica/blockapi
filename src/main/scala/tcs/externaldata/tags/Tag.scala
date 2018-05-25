package tcs.custom

import org.bitcoinj.core.Address

import scala.collection.mutable
import scala.io.Source

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.{validator, _}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import scala.collection.mutable.ListBuffer
import java.io._

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
   
  def getTags() : Unit = {

    val browser = JsoupBrowser ()

    var setDimension = 0
    var categoria = 1
    val writer = new BufferedWriter (new FileWriter ("tags.txt"))

    do {

      categoria += categoria
      println ("FILTER = " + categoria)
      var offset = 0 //Imposto offset

      do {

        setDimension = 0 //Reset dimensione set caricato

        println ("OFFSET = " + offset)
        val URL = "https://blockchain.info/it/tags?filter=" + categoria + "&offset=" + offset
        val htmlPage = browser.get (URL)
        
        //Estrapolazione Address e Link dalla pagina html
        val rawAddressLinks = htmlPage >> elementList ("td") >?> texts ("a")
        //Estrapolazione Tag dalla pagina html
        val rawTags = htmlPage >> elementList ("td") >?> texts ("span")
        //Estrapolazione Verified dalla pagina html
        val rawVerified = htmlPage >> elementList ("td") >?> attr ("src") ("img")

        var address = new ListBuffer[String] ()
        var links = new ListBuffer[String] ()
        var tags = new ListBuffer[String] ()
        var verified = new ListBuffer[String] ()

        //Popolamento lista Address e lista Link
        for (innerlist <- rawAddressLinks) {
          for (a <- innerlist) {
            for (value <- a) {
              if (value.isEmpty) { //se non è presente un link viene inserita la stringa empty
                links += "empty"
              }
              else {
                if (value.startsWith ("http") )
                {
                  links += value
                }
                else {
                  address += value
                }
              }

            }
          }
        }
        setDimension = address.length //Memorizzo dimensione set caricato

        //Popolamento lista Tag
        for (innerlist <- rawTags) {
          for (value <- innerlist) {
            for (a <- value) {
              tags += a
            }
          }
        }

        //Popolamento lista Verified
        for (innerlist <- rawVerified) {
          for (value <- innerlist) {
            if (value.toString.contains("red_cross") ) {
              verified += "0"
            }
            else {
              if (value.toString.contains ("green_tick") ) {
                verified += "1"
             }
            }
          }
        }

        //Impostazione categoria caricata
        val category = categoria match {
          case 2 => "BitcoinTalk Profiles"
          case 4 => "Bitcoin-OTC Profiles"
          case 8 => "Submitted Links"
          case 16 => "Signed Messages"
          case 32 => "Unknow Category"
          case _ => "Invalid Category: " + categoria
        }

        //Effettuo scrittura su file
        for (a <- 0 to address.length - 1) {
          writer.write (address (a) + "," + category + "," + tags (a) + "," + verified (a) + "," + links (a) + "\n")
         }

        //Incremento l'offset per il caricare il prossimo set
        offset = offset + 50
      } while (setDimension >= 50) // Il ciclo viene eseguito fino a quando vi sono dei dati estratti
    } while (categoria <= 16) // Effettuo il caricamento per ogni categoria
    writer.close () // Chiusura Buffer

    println ("Procedura completata")
  }
 
}
