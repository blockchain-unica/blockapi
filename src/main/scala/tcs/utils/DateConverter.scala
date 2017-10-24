package tcs.utils

import java.text.SimpleDateFormat

/**
  * Created by Livio on 26/09/2017.
  */
object DateConverter {

  def convertDate(date: java.util.Date) : String = {
    var formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return formatter.format(date)
  }

  def formatTimestamp(ms: Long) : String = "[" + new SimpleDateFormat("HH:mm:ss").format(ms) + "]"

}
