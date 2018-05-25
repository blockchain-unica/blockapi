package tcs.externaldata.rates

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.util.Date

import com.codesnippets4all.json.parsers.JsonParserFactory
import org.apache.commons.lang3.time.DateUtils

import scala.io.Source

/**Output to be checked**/

object LitecoinRates {

  var lastDate: Date = new Date(0)
  var price: Double = 0


  def getRate(date : Date): Double = {

    if(date.before(new Date(1438905600))) return 0  // Coindesk has no rates before this timestamp

    if (!DateUtils.isSameDay(date, lastDate)) {
      lastDate = date

      try {
        price = getPriceHistorical(date)
      } catch {
        case e: IOException =>
          return 0
      }
    }

    return price
  }


  private def getPriceHistorical(time: Date): Double = {
//TODO CHECK DATA ENDPOINT
    val url = new URL("https://min-api.cryptocompare.com/data/pricehistorical?fsym=LTC&tsyms=USD&ts=" + time.getTime.toString.dropRight(3) + "&markets=Coinbase")
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    val br = new BufferedReader(new InputStreamReader(connection.getInputStream))
    val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")

    val factory = JsonParserFactory.getInstance
    val parser = factory.newJsonParser
    val map = parser.parseJson(str)
    val usd = map.get("LTC").asInstanceOf[java.util.HashMap[String, String]]

    return parseDouble(usd.get("USD"))

  }


  private def parseDouble(s: String) : Double =
    try {
      Some(s.toDouble)
      s.toDouble
    } catch { case _ : Throwable => 0d }
}
