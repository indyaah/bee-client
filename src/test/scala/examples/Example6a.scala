package examples

import uk.co.bigbeeconsultants.http._
import header.{HeaderName, Headers, Credential}

object Example6a extends App {
  val url = "http://beeclient/private/lorem2.txt"
  val httpClient = new HttpClient
  val response401 = httpClient.get(url) // no authentication
  println(response401.status)
  println(response401.headers(HeaderName.WWW_AUTHENTICATE).toAuthenticateValue)

  val credential = new Credential("bigbee", "HelloWorld")
  val login = Headers(credential.basicAuthHeader)
  val response200 = httpClient.get(url, login) // with authentication
  println(response200.status)
  println(response200.body)
}
