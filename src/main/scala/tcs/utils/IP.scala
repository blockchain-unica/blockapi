package tcs.utils

import net.liftweb.json.{DefaultFormats, parse}
import scala.io.Source.fromURL

/**
  * This class has methods for analyzing IP addresses and obtaining related information
  */
class IP {

  /**
    * This method receives as input an ip address (string) and returns the name of the country of origin (string)
    * The APIs of GeoIP are used (https://geoip.nekudo.com/) are used for geolocation
    * The net.liftweb.json libraries are used to parse the data
    * GeoIP can not always associate a country with a specific IP address:
    * in this case, the code handles the MappingException exception
    * that occurs when a given json: "type: error" is returned
    *
    * @param ipaddress ('relayed_by' field from blockcypher)
    * @return country
    */
  def getCountry(ipaddress: String): String = {

    // case class country json extract
    case class Country(name: String, code: String)
    case class Location(accuracy_radius: Number, latitude: Number, longitude: Number, time_zone: String)
    case class jsonOut(city: Boolean, country: Country, location: Location, ip: String)

    implicit val formats = DefaultFormats

    val urlAPIGeoIp = "http://geoip.nekudo.com/api/"

    /*
    An ip (ipaddress) of value "0" is supplied as input to the function when this is not recognized as a valid ipv4 (ipv6)
     */
    if (ipaddress != "0") {
      val urlComplete: String = urlAPIGeoIp + ipaddress.mkString
      val result = fromURL(urlComplete).mkString

      // json object parsing
      val jsonValue = parse(result)

      // try to parse the json object
      try {
        val country = ((jsonValue \ "country") \ "name").extract[String]
        return country
      }
      // handles the exception that occurs when geoip does not associate a nation with the ip
      catch {
        case ex: net.liftweb.json.MappingException => {
          return "0"
        }
      }

    }
    // returns "0" when ipaddress received in input is "0"
    else return "0"
  }
}


