package tcs.custom

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.util.Date

import com.codesnippets4all.json.parsers.JsonParserFactory
import org.apache.commons.lang3.time.DateUtils
import java.text.SimpleDateFormat

/**
  * Created by Livio on 13/06/2017.
  */
object Exchange {
  var lastDate: Date = new Date(0)
  var price: Double = 0

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
}
