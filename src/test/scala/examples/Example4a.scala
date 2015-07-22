package examples

import uk.co.bigbeeconsultants.http._
import header.{Cookie, CookieJar}

object Example4a {
  val cookie = Cookie(name = "XYZ", value = "zzz", domain = "localhost")
  val originalCookieJar = CookieJar(cookie)
  val url = "http://httpbin.org/get"
  val httpClient = new HttpClient(Config(connectTimeout = 60000))

  def main(args: Array[String]) {
    val response = httpClient.get(url, Nil, originalCookieJar)
    println(response.body)
    val augmentedCookieJar = response.cookies.get
    println(augmentedCookieJar)
    println(augmentedCookieJar.get("XYZ").get.value) // prints "zzz"
  }
}
