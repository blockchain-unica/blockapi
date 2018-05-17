package tcs.custom.ethereum

import scala.io._
import java.util._
import java.io._

/**
  * Created by Gerina Federica, Massa Silvia and Moi Francesca on 07/05/2018.
  *
  * The "State of the Dapps" site offers information on a list of Ethereum smart contracts,
  * called from the site DApps (decentralized applications). For example, the status (prototype, work in
  * progress,live, ...), the name, the address of the Ethereum contract are shown.
  *
  * Our object contains methods to extracts this information from the site for each DApp and
  * puts it into a file called dapps.txt.
  *
  * The DApps list and the respective information are extracted from the site via HTTP request,
  * using a query string, and some regular expression.
  *
  * The main method is getDappsInFile, that calls the smaller methods to retrieve the complete list of the
  * DApps, extracts the information for each DApp of the list and writes it in a text file.
  *
  **/

object StateOfTheDapps {

  /*
   * getNumberOfDapps
   * @return the number of DApps in the site
   */
  def getNumberOfDapps() : Int = {
    val dappsHtml = Source.fromURL("https://www.stateofthedapps.com/dapps/").mkString
    val countStr = "\"dappCount\":.*?,".r.findFirstIn(dappsHtml).toString
    val numberOfDapps = Integer.parseInt(countStr.substring(17, countStr.length()-2))
    return numberOfDapps
  }

  /*
   * getAllDapps
   * @param numberOfDapps the total number of the DApps
   * @return an ArrayList with all the DApps present in the site
   */
  def getAllDapps(numberOfDapps:Int):ArrayList[String] = {
    val mainHtml = Source.fromURL("https://api.stateofthedapps.com/dapps?limit=" + numberOfDapps + "&text=%20").mkString
    val links = new ArrayList[String]
    val regex = "\"slug\": \".*?\"".r
    for ( link <-regex.findAllIn(mainHtml)) {
      links.add(link.substring(9,link.length()-1))
    }
    return links
  }

  /*
   * getDappsByCategory
   * @param numberOfDapps the total number of the DApps
   * @param category the string that identifies the category of the DApps
   * @return all the DApps considered in the category passed as parameter in an ArrayList
   */
  def getDappsByCategory(numberOfDapps:Int, category:String):ArrayList[String] = {
    val categoryHtml = Source.fromURL("https://api.stateofthedapps.com/collections/" + category + "?limit=" + numberOfDapps).mkString
    val linksCategory = new ArrayList[String]
    val regex = "\"slug\": \".*?\"".r
    for ( link <-regex.findAllIn(categoryHtml)) {
      linksCategory.add(link.substring(9,link.length()-1))
    }
    return linksCategory
  }

  /*
   * getDappsInFile
   * @return a file called dapps.txt that contains (in this specific order):
   * first name; author; state; Description; date on which the project was sent to the site;
   * software license; list of associated tags; category (finance, gaming, ...); address of the Ethereum contract.
   */
  def getDappsInFile() {
    try{
      val numberOfDapps = this.getNumberOfDapps()
      val links = this.getAllDapps(numberOfDapps)
      val linksFinance = this.getDappsByCategory(numberOfDapps, "finance")
      val linksGaming = this.getDappsByCategory(numberOfDapps, "cryptogaming")

      val pw = new PrintWriter(new File("queries/stateofthedapps/dapps.txt" ))

      var index = 0
      for (index <- 0 to links.size()-1) {
        val dappHtml = Source.fromURL("https://www.stateofthedapps.com/dapps/" + links.get(index)).mkString

        val namePattern = "\"name\":\".*?\"".r.findFirstIn(dappHtml).getOrElse("")
        val finalNamePattern = namePattern.substring(7,namePattern.length())

        val authorsPattern = "\"authors\".*?]".r.findFirstIn(dappHtml).getOrElse("")
        val finalAuthorsPattern = authorsPattern.substring(11,authorsPattern.length()-1)

        val statusPattern = "\"status\":\".*?\"".r.findFirstIn(dappHtml).getOrElse("")
        val finalStatusPattern = statusPattern.substring(9,statusPattern.length())

        val descriptionPattern = "\"description\":\".*?\"".r.findFirstIn(dappHtml).getOrElse("")
        val finalDescriptionPattern = descriptionPattern.substring(14,descriptionPattern.length())

        val createdPattern = "\"created\":\".*?\"".r.findFirstIn(dappHtml).getOrElse("")
        val finalCreatedPattern = createdPattern.substring(10,createdPattern.length())

        val licensePattern = "\"license\":\".*?\"".r.findFirstIn(dappHtml).getOrElse("")
        val finalLicensePattern = licensePattern.substring(10,licensePattern.length())

        val tagsPattern = "\"tags\".*?]".r.findFirstIn(dappHtml).getOrElse("")
        val tagsPatternWithoutTag = tagsPattern.substring(8,tagsPattern.length()-1)
        val tagsPatternWithoutDoubleQuotes = tagsPatternWithoutTag.replaceAll("\"","")
        val finalTagsPattern = "\"" + tagsPatternWithoutDoubleQuotes + "\""

        var category = ""
        if (linksGaming.contains(links.get(index)))
          category = "cryptogaming"

        if (linksFinance.contains(links.get(index)))
          category = "finance"

        val addressPattern = "\"address\":\".*?\"".r.findFirstIn(dappHtml).getOrElse("")
        val finalAddressPattern = addressPattern.substring(10,addressPattern.length())

        val match1 = Array(finalNamePattern + ",", finalAuthorsPattern + ",", finalStatusPattern + ",", finalDescriptionPattern + ",", finalCreatedPattern + ",",
          finalLicensePattern + ",", finalTagsPattern + ",", "\"" + category + "\", ", finalAddressPattern + ";")

        for (line <- match1) {
          pw.print(line )
        }
        pw.println("\r\n")
      }

      pw.close()
    }catch {
      case ioe: java.io.IOException => ioe.printStackTrace(); println("Error: IO Exception")
      case fnf: java.io.FileNotFoundException => fnf.printStackTrace(); println("Error: File Not Found Exception")
      case e: Exception => e.printStackTrace(); println("Error: Exception")}
  }
}