package examples

import uk.co.bigbeeconsultants.http._
import auth.{Credential, CredentialSuite}
import header.{Headers, CookieJar, HeaderName}

object Example6b extends App {
  val url = "http://beeclient/private/lorem2.txt"
  val credential = new Credential("bigbee", "HelloWorld")
  val suite = new CredentialSuite(Map("Restricted" -> credential))
  val browser = new HttpBrowser(credentialSuite = suite)
  val response200 = browser.get(url)
  println(response200.status)
  println(response200.body)
}
