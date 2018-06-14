package it.unica.blockchain.externaldata.rates

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.text.SimpleDateFormat
import java.util.Date

import com.codesnippets4all.json.parsers.JsonParserFactory
import org.apache.commons.lang3.time.DateUtils

/**
  * Created by Livio on 13/06/2017.
  */
object BitcoinRates {
  var lastDate: Date = new Date(0)
  var price: Double = 0
  var prices = scala.collection.mutable.Map("USD"->0.0, "EUR"->0.0, "GBP"->0.0, "JPY"->0.0, "CNY"->0.0)
  val managedCurrencies: List[String] = List("USD", "EUR", "GBP", "JPY", "CNY")

  def getRate(date: Date): Double = {

    // Coindesk has no rates before this timestamp
    if (date.before(new Date(1279411200000l))) return 0

    if (!DateUtils.isSameDay(date, lastDate)) {
      lastDate = date

      try {

        val format = new SimpleDateFormat("yyyy-MM-dd")
        val sDate = format.format(date)

        var url = new URL("http://api.coindesk.com/v1/bpi/historical/close.json?start=" + sDate + "&end=" + sDate)
        var connection = url.openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("GET")
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        val br = new BufferedReader(new InputStreamReader(connection.getInputStream))

        val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")

        val factory = JsonParserFactory.getInstance
        val parser = factory.newJsonParser
        val map = parser.parseJson(str)
        val bpi = map.get("bpi").asInstanceOf[java.util.Map[String, String]]
        price = bpi.get(sDate).toDouble

      } catch {
        case e: IOException =>
          return 0
      }
    }

    return price
  }

  def getRate(date: Date, currency: String): Double = {

    // Coindesk has no rates before this timestamp
    if (date.before(new Date(1279411200000l))) return 0
    if (!managedCurrencies.contains(currency)) return 0

    if (DateUtils.isSameDay(date, lastDate) && prices(currency)==0) {
      try {

        val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val sDate = format.format(date)

        var url = new URL("https://rest.coinapi.io/v1/exchangerate/BTC/" + currency + "?apikey=926AB507-0C4B-4122-AA7A-1823464AE3D5&time=" + sDate)

        var connection = url.openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("GET")
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")

        if (connection.getResponseCode() == 550){ //Check for unaviable data response
          return 0
        }

        val br = new BufferedReader(new InputStreamReader(connection.getInputStream))
        val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")
        val factory = JsonParserFactory.getInstance
        val parser = factory.newJsonParser
        val map = parser.parseJson(str)

        prices(currency) = map.get("rate").asInstanceOf[String].toDouble

      } catch {
        case e: IOException =>
          return 0
      }
    }
    else{
      if (!DateUtils.isSameDay(date, lastDate)) {
        lastDate = date
        try {

          val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
          val sDate = format.format(date)
          var url = new URL("https://rest.coinapi.io/v1/exchangerate/BTC/" + currency + "?apikey=926AB507-0C4B-4122-AA7A-1823464AE3D5&time=" + sDate)

          var connection = url.openConnection().asInstanceOf[HttpURLConnection]
          connection.setRequestMethod("GET")
          connection.setRequestProperty("User-Agent", "Mozilla/5.0")

          if (connection.getResponseCode() == 550){
            return 0
          }

          val br = new BufferedReader(new InputStreamReader(connection.getInputStream))
          val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")

          val factory = JsonParserFactory.getInstance
          val parser = factory.newJsonParser
          val map = parser.parseJson(str)
          prices(currency) = map.get("rate").asInstanceOf[String].toDouble

        } catch {
          case e: IOException =>
            return 0
        }
      }
    }
    return prices(currency)
  }
}
