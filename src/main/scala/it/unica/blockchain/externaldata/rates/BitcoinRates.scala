package it.unica.blockchain.externaldata.rates

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.codesnippets4all.json.parsers.JsonParserFactory
import org.apache.commons.lang3.time.DateUtils
import play.api.libs.json.Json
import scalaj.http.Http

import scala.collection.mutable

/**
  * Created by Livio on 13/06/2017.
  */
object BitcoinRates {
  var lastDate: Date = new Date(0)
  var price: Double = 0
  var prices = scala.collection.mutable.Map("USD"->0.0, "EUR"->0.0, "GBP"->0.0, "JPY"->0.0, "CNY"->0.0)
  val managedCurrencies: List[String] = List("USD", "EUR", "GBP", "JPY", "CNY")

  var BTCtoUSD_JSON = new String
  var USDToOther_JSON = new String

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

  def getRate_Mod(date: Date): mutable.Map[String, Double] = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    var date_form = dateFormat.format(date)
    var USDRate, EURRate, GBPRate, JPYRate, CNYRate : Double = 0

    //Getting the equivalent Calendar of the Date
    val cal = Calendar.getInstance()
    cal.setTime(date)

    // Coindesk has no rates before this timestamp
    if (date.before(new Date(1279411200000l))) return prices

    val BTCtoUSD = Json.parse(BTCtoUSD_JSON)
    USDRate = (BTCtoUSD \ "bpi" \ date_form).get.as[Double]

    //On Saturday and Sunday Banks close. For that, there is no exchange data for those days
    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
      cal.add(Calendar.DATE, -1)
      date_form = dateFormat.format(cal.getTime)
    }
    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
      cal.add(Calendar.DATE, -2)
      date_form = dateFormat.format(cal.getTime)
    }

    val USDToOther = Json.parse(USDToOther_JSON)
    EURRate = (USDToOther \ "rates" \ date_form \ "EUR").get.as[Double] * USDRate
    GBPRate = (USDToOther \ "rates" \ date_form \ "GBP").get.as[Double] * USDRate
    JPYRate = (USDToOther \ "rates" \ date_form \ "JPY").get.as[Double] * USDRate
    CNYRate = (USDToOther \ "rates" \ date_form \ "CNY").get.as[Double] * USDRate

    prices += ("USD" -> USDRate)
    prices += ("EUR" -> EURRate)
    prices += ("GBP" -> GBPRate)
    prices += ("JPY" -> JPYRate)
    prices += ("CNY" -> CNYRate)

    return prices
  }

  def setRate(start: Date, end: Date) = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    var start_string = dateFormat.format(start)
    val end_string = dateFormat.format(end)

    //Getting the equivalent Calendar of the Date
    val cal = Calendar.getInstance()
    cal.setTime(start)
    //On Saturday and Sunday Banks close. For that, there is no exchange data for those days
    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
      cal.add(Calendar.DATE, -1)
      start_string = dateFormat.format(cal.getTime)
    }
    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
      cal.add(Calendar.DATE, -2)
      start_string = dateFormat.format(cal.getTime)
    }

    // Coindesk has no rates before this timestamp
    if (start.before(new Date(1279411200000l))) start_string = "2010-07-17"
    BTCtoUSD_JSON = Http("https://api.coindesk.com/v1/bpi/historical/close.json?start="+ start_string +"&end="+ end_string).asString.body
    if (start.before(new Date(1279411200000l))) start_string = "2010-07-16"
    USDToOther_JSON = Http("https://api.exchangeratesapi.io/history?base=USD&symbols=EUR,GBP,JPY,CNY&start_at="+ start_string +"&end_at="+ end_string).asString.body
  }
}
