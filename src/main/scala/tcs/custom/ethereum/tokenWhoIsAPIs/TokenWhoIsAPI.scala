package tcs.custom.ethereum.tokenWhoIsAPIs

import java.security.SecureRandom
import javax.net.ssl.{HttpsURLConnection, SSLContext}

import scalaj.http.Http
import tcs.custom.ethereum.Utils

object TokenWhoIsAPI {

  def getUsedBlockchain(tokenName: String) = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      this.sendRequest(tokenName)
    ).blockchain
  }

  private def sendRequest(tokenName: String): String = {
    try {
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, Utils.trustAllCerts, new SecureRandom)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
      Http(
        String.join(
          "/", "http://tokenwhois.com/api/projects", tokenName
        )
      ).asString.body
    } catch {
      case e: Exception =>
        System.out.println(e)
        ""
    }
  }
}
