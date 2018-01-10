package tcs.custom.ethereum

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._


object Exchanges {
  private var exchangesList : List[Exchange] = List[Exchange]()
  private val browser = JsoupBrowser()

  private def extractExchanges: Unit = {
    val exchangesDoc = browser.get("https://www.walletexplorer.com/") >> elementList(".serviceslist td")
    val exchangesListHtml =
      exchangesDoc.filter((element) => (element >> allText("h3")).contains("Exchanges")).head >>
        elementList("ul li")
    val exchangesNames = exchangesListHtml.map((element) => element >> allText("li a"))
    exchangesList = exchangesNames.map((exchange) => new Exchange(exchange))
  }

  def getExchange(walletAddress: String): String = {
    if(exchangesList.isEmpty){
      extractExchanges
    }
    val walletExchangeDoc = browser.get("https://www.walletexplorer.com/address/" + walletAddress) >> elementList(".walletnote")
    val exchangeLink = walletExchangeDoc >> allText("a")
    exchangeLink.head
  }
}
