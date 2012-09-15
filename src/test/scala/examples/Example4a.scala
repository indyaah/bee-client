package examples

import uk.co.bigbeeconsultants.http._
import header.{Cookie, CookieJar}

object Example4a extends App {
  val cookie = Cookie (name = "XYZ", value = "zzz", domain = "localhost")
  val originalCookieJar = CookieJar(cookie)
  val url = "http://localhost/lighthttpclient/test-echo-back.php"
  val httpClient = new HttpClient
  val response = httpClient.get(url, Nil, originalCookieJar)
  println(response.body)
  val augmentedCookieJar = response.cookies.get
  println(augmentedCookieJar)
}
