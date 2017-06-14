package tcs.custom

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.util.Date

import com.codesnippets4all.json.parsers.JsonParserFactory
import org.apache.commons.lang3.time.DateUtils

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
        var line: String = null

        var exit = false
        while (exit) {
          reader.readLine() match {
            case line: String => text.append(line);
            case null => exit = true;
          }
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
