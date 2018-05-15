package tcs.custom.litecoin

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._


object Exchanges {
  private val browser = JsoupBrowser()

  def getExchange(walletAddress: String): String = {
    val walletExchangeDoc = browser.get("https://www.walletexplorer.com/address/" + walletAddress) >> elementList(".walletnote")
    val exchangeLink = walletExchangeDoc >> allText("a")
    exchangeLink.head
  }

  def getWallets(exchangeName: String): List[String] = {
    this.getWallets(exchangeName, 0)
  }

  def getWallets(exchangeName: String, limit: Int): List[String] = {
    var wallets: List[String] = List()
    var pageIndex: Int = 1
    var end = false
    while(!end){
      var exchangeWalletsDoc: List[String] =
        browser.get("https://www.walletexplorer.com/wallet/" + exchangeName + "/addresses?page=" + pageIndex) >>
          elementList("table a") >> allText("a")
      if(limit != 0 && wallets.lengthCompare(limit) >= 0){
        return wallets.take(limit)
      }
      end = exchangeWalletsDoc == Nil
      wallets = wallets ::: exchangeWalletsDoc
      pageIndex+=1
      println(pageIndex)
    }
    wallets
  }
}
