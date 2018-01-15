package tcs.custom.ethereum

import scalaj.http.Http

object ICOBenchAPI {
  private val privateKey	= "private-key"
  private val publicKey	= "public-key"
  private val apiUrl		= "https://icobench.com/api/v1/"


  def getICO(icoID: String = "all"): Unit = {
    Http(this.apiUrl + "icos/" + icoID).postData("{}")
  }
}
