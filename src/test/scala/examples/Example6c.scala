package examples

import uk.co.bigbeeconsultants.http._
import header.HeaderName

object Example6c extends App {
  val url = "http://beeclient/digest/lorem3.txt"
  val httpClient = new HttpClient
  val response401 = httpClient.get(url) // no authentication
  println(response401.status)
  val authenticate = response401.headers(HeaderName.WWW_AUTHENTICATE).toAuthenticateValue
  println(authenticate)
  println(authenticate.realm)
  println(authenticate.domain)
  println(authenticate.nonce)
  println(authenticate.opaque)
  println(authenticate.stale)
  println(authenticate.algorithm)
  println(authenticate.qop)
  println(authenticate.isValid)

//  val credential = new Credential("bigbee", "HelloWorld")
//  val login = Headers(credential.basicAuthHeader)
//  val response200 = httpClient.get(url, login) // with authentication
//  println(response200.status)
//  println(response200.body)
}
