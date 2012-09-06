package examples

import uk.co.bigbeeconsultants.http._
import header.{Cookie, CookieJar}

object Example4a extends App {
  val cookie = Cookie (name = "XYZ", string = "zzz", domain = "localhost")
  val cookieJar = CookieJar(cookie)
  val url = "http://localhost/lighthttpclient/test-echo-back.php"
  val httpClient = new HttpClient
  val response = httpClient.get(url, Nil, cookieJar)
  println(response.body)
}
