package tcs.custom.ethereum.ICOAPIs.ICORatingAPIs

import java.security.SecureRandom
import javax.net.ssl.{HttpsURLConnection, SSLContext}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{allText, elementList}

import tcs.custom.ethereum.Utils

object ICORatingAPI {

  private var browser: JsoupDocument = _

  /**
    * @param tokenName
    * @return token hype score
    */
  def getHypeScore(tokenName: String): Float = {
    val score = multipleRequest("Hype", tokenName)
    var scoreString = score.asInstanceOf[String]
    if(scoreString.nonEmpty){
      scoreString = scoreString.substring(0, scoreString.indexOf("/"))
      val scoreFloat = Option[Float](scoreString.toFloat)
      if (scoreFloat.isEmpty) {
        return 0
      }
      return scoreFloat.get
    }
    0
  }

  /**
    * @param tokenName
    * @return token investment rating
    */
  def getInvestmentRating(tokenName: String): String = {
    multipleRequest("Investment", tokenName)
  }

  /**
    * @param tokenName
    * @return token risk score
    */
  def getRiskScore(tokenName: String): Float = {
    val score = multipleRequest("Risk", tokenName)
    var scoreString = score.asInstanceOf[String]
    if(scoreString.nonEmpty){
      scoreString = scoreString.substring(0, scoreString.indexOf("/"))
      val scoreFloat = Option[Float](scoreString.toFloat)
      if (scoreFloat.isEmpty) {
        return 0
      }
      return scoreFloat.get
    }
    0
  }

  private def multipleRequest(scoreType: String, tokenName: String): String = {
    var found: Boolean = false
    var response: String = ""
    Utils.prepareNames(tokenName).iterator.takeWhile(_ => !found).foreach(name => {
      response = getScore(scoreType, name)
      if(response.nonEmpty){
        found = true
      }
    })
    response
  }

  private def getScore(scoreType: String, tokenName: String): String = {
    try {
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, Utils.trustAllCerts, new SecureRandom)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
      initializeBrowser(String.join("/", "https://icorating.com/ico", tokenName))
      val scoreParts = this.browser >> elementList("div .white-block-area div div")
      val scoreDoc = scoreParts.filter(element => {
        (element >> allText(".title")).contains(scoreType)
      }).head
      var score = scoreDoc >> allText(".score")
      if (score.isEmpty) {
        score = scoreDoc >> allText(".name")
      }
      score
    } catch {
      case e: Exception =>
        System.out.println(e)
        ""
    }
  }

  private def initializeBrowser(page: String): Unit = {
    if (!(this.browser == null)) {
      if (!this.browser.underlying.baseUri.equals(page)) {
        this.browser = new JsoupBrowser().get(page)
      }
    } else {
      this.browser = new JsoupBrowser().get(page)
    }
  }
}
