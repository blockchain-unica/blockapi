package tcs.custom

import java.util.Date

import org.apache.commons.lang3.time.DateUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.{HttpURLConnection, URL}

import com.codesnippets4all.json.parsers.JsonParserFactory

/**
  * Created by Livio on 13/06/2017.
  */
object Exchange {
  var lastDate: Date = new Date(0)
  var price: Double = 0

  def getRate(date: Date): Double = {

    // Coindesk has no rates before this timestamp
    if (date.before(new Date(1279324800))) return 0

    if (!DateUtils.isSameDay(date, lastDate)) {
      lastDate = date

      var text: StringBuffer = new StringBuffer();
      try {
        var url = new URL("http://api.coindesk.com/v1/bpi/historical/close.json?start=" + date + "&end=" + date);
        var connection: HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection];
        connection.setRequestMethod("GET");
        val reader = new BufferedReader(new InputStreamReader(connection.getInputStream))
        var line : String = null

        while ((line = reader.readLine()) != null) {
          text.append(line);
        }
      } catch {
        case e: IOException =>
          return 0
      }
      val factory = JsonParserFactory.getInstance
      val parser = factory.newJsonParser
      val map = parser.parseJson(text.toString)
      val bpi = map.get("bpi").asInstanceOf[java.util.Map[String, String]]
      price = bpi.get(date).toDouble
    }

    return price
  }
}
