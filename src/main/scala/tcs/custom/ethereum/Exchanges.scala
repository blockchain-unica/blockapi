package tcs.custom.ethereum

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._


object Exchanges {
  private var exchangesList : List[Exchange] = List[Exchange]()

  private def extractExchanges: Unit = {
    val browser = JsoupBrowser()
    val exchangesDoc = browser.get("https://www.walletexplorer.com/") >> elementList(".serviceslist td")
    val exchangesListHtml =
      exchangesDoc.filter((element) => (element >> allText("h3")).contains("Exchanges")).head >>
        elementList("ul li")
    val exchangesNames = exchangesListHtml.map((element) => element >> allText("li a"))
    exchangesList = exchangesNames.map((exchange) => new Exchange(exchange))
    println(exchangesList)
  }

  def getExchange(walletAddress: String): Unit = {
    if(exchangesList.isEmpty){
      extractExchanges
    }
  }
}
