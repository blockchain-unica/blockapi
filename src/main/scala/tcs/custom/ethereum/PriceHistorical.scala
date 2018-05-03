package tcs.custom.ethereum

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.util.Date

import com.codesnippets4all.json.parsers.JsonParserFactory
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.commons.lang3.time.DateUtils
import tcs.custom.bitcoin.Exchange.lastDate
import tcs.pojos.{CoinMarketPrices, CoinMarketPricesRaw}

import scala.io.Source

/**
  * Created by ferruvich on 16/08/2017.
  */
object PriceHistorical {

  var lastDate: Date = new Date(0)
  var price: Double = 0


  private var marketCap: List[MarketCap] = _

/*
  def getPriceHistorical(): CoinMarketPrices = {
    val url = new URL("https://graphs.coinmarketcap.com/currencies/ethereum/")
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    val br = new BufferedReader(new InputStreamReader(connection.getInputStream))
    val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val coinMarketRaw = mapper.readValue[CoinMarketPricesRaw](str)

    val format = new SimpleDateFormat("yyyy-MM-dd")

    val coinMarket = CoinMarketPrices(coinMarketRaw.market_cap_by_available_supply.map(l => format.format(new Date(l.head.longValue())) -> l.last.intValue()).toMap[Date, Int],
      coinMarketRaw.price_btc.map(l => format.format(new Date(l.head.longValue())) -> l.last.doubleValue()).toMap[Date, Double],
      coinMarketRaw.price_usd.map(l => format.format(new Date(l.head.longValue())) -> l.last.doubleValue()).toMap[Date, Double])
    coinMarket
  }
*/

  def getRate(date : Date): Double = {


    // Coindesk has no rates before this timestamp
    if(date.before(new Date(1438905600))) return 0

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

    val url = new URL("https://min-api.cryptocompare.com/data/pricehistorical?fsym=ETH&tsyms=USD&ts=" + time.getTime.toString.dropRight(3) + "&markets=Coinbase")
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    val br = new BufferedReader(new InputStreamReader(connection.getInputStream))
    val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")

    val factory = JsonParserFactory.getInstance
    val parser = factory.newJsonParser
    val map = parser.parseJson(str)
    val usd = map.get("ETH").asInstanceOf[java.util.HashMap[String, String]]

    return parseDouble(usd.get("USD"))

  }

  def getMarketCapList(): List[MarketCap] = {
    if(this.marketCap == null){
      prepareMarketCapList()
    }
    this.marketCap
  }


  private def prepareMarketCapList(): Unit = {
    val bufferedSource = Source.fromFile("src/main/scala/tcs/custom/regression/export-MarketCap.csv")
    this.marketCap = List[MarketCap]()
    val lines = bufferedSource.getLines()
    lines.foreach(line => {
      val cols = line.split(",")
      if(!cols(0).contains("Date")){
        this.marketCap ::= new MarketCap(cols(0).replace("\"", ""), BigDecimal(cols(1).replace("\"", "")), BigDecimal(cols(2).replace("\"", "")), BigDecimal(cols(3).replace("\"", "")))
      }
    })
  }

  private def parseDouble(s: String) : Double =
    try {
      Some(s.toDouble)
      s.toDouble
    } catch { case _ => 0d }
}
