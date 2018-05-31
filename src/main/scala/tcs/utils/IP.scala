package tcs.utils

import net.liftweb.json.{DefaultFormats, parse}
import tcs.blockchain.bitcoin._

import scala.io.Source.fromURL
import scala.util.matching.Regex

class IP {

  /**
    * @param ipaddress ('relayed_by' field from blockcypher)
    * @return country
    *
    */
  def getCountry(ipaddress: String): String = {

    // case class country json extract
    case class Country(name: String, code: String)
    case class Location(accuracy_radius: Number, latitude: Number, longitude: Number, time_zone: String)
    case class jsonOut(city: Boolean, country: Country, location: Location, ip: String)

    implicit val formats = DefaultFormats

    val urlAPIGeoIp = "http://geoip.nekudo.com/api/"

    if (ipaddress != "0") {
      val urlComplete: String = urlAPIGeoIp + ipaddress.mkString
      val result = fromURL(urlComplete).mkString

      // json object parsing
      val jsonValue = parse(result)

      // method for extract country name (liftweb api)
      val country = ((jsonValue \ "country") \ "name").extract[String]

      return country
    }

    return "0"




  }
}