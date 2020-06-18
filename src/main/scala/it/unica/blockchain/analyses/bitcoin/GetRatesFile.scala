package it.unica.blockchain.analyses.bitcoin

import java.io.{BufferedWriter, FileWriter}
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneOffset}

import it.unica.blockchain.externaldata.rates.BitcoinRates


object GetRatesFile {
  def main(args: Array[String]): Unit = {

    //first day with an exchange rate
    val startDate = LocalDate.of(2010, 8, 18)

    val nowDate = LocalDate.now()

    val numberDays = startDate.until(nowDate, ChronoUnit.DAYS).toInt

    val bw = new BufferedWriter(new FileWriter("rates.csv"))

    (0 to numberDays)
      .map(days => startDate.plusDays(days))
      .foreach(d => {
        val date = java.util.Date.from(d.atStartOfDay().toInstant(ZoneOffset.UTC))
        val res = date.getTime + ", " + BitcoinRates.getRate(date)
        bw.write(res)
      })

    println("Done")

    bw.close()
  }
}
