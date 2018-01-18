package tcs.custom.ethereum.tokenWhoIsAPIs

import scalaj.http.Http

import tcs.custom.ethereum.Utils

object TokenWhoIsAPI {

  private def sendRequest(tokenName: String) = {
    Utils.getMapper.readValue[TokenWhoIsResponse](
      Http(
        String.join(
          "/", "http://tokenwhois.com/api/projects/", tokenName
        )
      ).asString.body
    )
  }
}
