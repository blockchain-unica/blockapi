package tcs.utils.converter

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

/**
  * Created by Livio on 26/09/2017.
  */
object DateConverter {

  def convertDate(date: java.util.Date) : String = {
    var formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return formatter.format(date)
  }

  def formatTimestamp(ms: Long) : String = "[" + new SimpleDateFormat("HH:mm:ss").format(ms) + "]"


  def getDateFromTimestamp(timestamp : BigInt) : Date = {

    val ts = timestamp.longValue() * 1000L
    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val stringDate = df.format(ts)
    return df.parse(stringDate)
  }

  def getCalendarFromDate(date : Date) : Calendar = {
    val c = Calendar.getInstance
    c.setTime(date)
    c
  }
}
