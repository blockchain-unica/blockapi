package it.unica.blockchain.custom

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
object Tag{

  // TODO: Update in order to support blockchaininfoTags.txt file
  def getTagsFromFile(fileName: String): mutable.TreeMap[String, String] = {
    var map = mutable.TreeMap.empty[String, String]

    val bufferedSource = Source.fromFile(fileName)
    for (line <- bufferedSource.getLines) {
      var strings: Array[String] = line.toString.split(", ")
      map += (strings(0) -> strings(1))
    }

    bufferedSource.close
    return map
  }


  def getTagsFromBlockchainInfo() : Unit = {

    val browser = JsoupBrowser ()

    var setDimension = 0
    var category = 1
    var results = 50  // Each page has 50 results
    val writer = new BufferedWriter (new FileWriter ("src/main/scala/tcs/externaldata/tags/blockchaininfoTags.txt"))

    do {

      category += category
      println ("FILTER = " + category)
      var offset = 0    // Offset for browsing between pages

      do {

        var setDimension = 0 // Reset dimension
        println ("OFFSET = " + offset)

        val URL = "https://blockchain.info/it/tags?filter=" + category + "&offset=" + offset
        val htmlPage = browser.get (URL)
        
        // Parsing html page: extract Address e Link
        val rawAddressLinks = htmlPage >> elementList ("td") >?> texts ("a")

        // Parsing html page: extract Tag
        val rawTags = htmlPage >> elementList ("td") >?> texts ("span")

        // Parsing html page: extract Verified
        val rawVerified = htmlPage >> elementList ("td") >?> attr ("src") ("img")

        var address = new ListBuffer[String] ()
        var links = new ListBuffer[String] ()
        var tags = new ListBuffer[String] ()
        var verified = new ListBuffer[String] ()

        // Populate Address and Link lists
        for (innerlist <- rawAddressLinks) {
          for (a <- innerlist) {
            for (value <- a) {
              if (value.isEmpty) { // Insert either URL or "empty"
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
        setDimension = address.length // Store set size

        // Populate Tag list
        for (innerlist <- rawTags) {
          for (value <- innerlist) {
            for (a <- value) {
              tags += a
            }
          }
        }

        // Populate Verified list
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

        // Set name of the current category
        val categoryName = category match {
          case 2 => "BitcoinTalk Profiles"
          case 4 => "Bitcoin-OTC Profiles"
          case 8 => "Submitted Links"
          case 16 => "Signed Messages"
          case _ => "Invalid Category: " + category
        }

        // Storing results in the target file
        for (a <- 0 to address.length - 1) {
          writer.write (address (a) + "," + categoryName + "," + tags (a) + "," + verified (a) + "," + links (a) + "\n")
         }

        // Increment offset for loading the next set
        offset = offset + results
      } while (setDimension >= results)
    } while (category <= 16) // Move between categories
    writer.close ()

    println ("Procedure completed")
  }
 
}
