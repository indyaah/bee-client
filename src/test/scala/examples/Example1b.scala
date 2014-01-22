package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.header.HeaderName._

object Example1b {
  // create a Config different from the default values
  val config = Config(connectTimeout = 10000,
    readTimeout = 5000,
    followRedirects = false)

  val httpClient = new HttpClient(config)

  def main(args: Array[String]) {
    val response = httpClient.get("http://x.org/")

    println(response.status.code) // prints "302"
    println(response.headers(LOCATION).value) // prints "/wiki/"
  }
}
