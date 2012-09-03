package examples

import uk.co.bigbeeconsultants.http.{Config, HttpClient}
import uk.co.bigbeeconsultants.http.HttpClient._
import uk.co.bigbeeconsultants.http.header.HeaderName

object Example1b extends App {
  // create a Config different from the default values
  val config = Config(connectTimeout = 10000,
    readTimeout = 1000,
    followRedirects = false)

  val httpClient = new HttpClient(config)
  val response = httpClient.get("http://www.x.org/")

  println(response.status.code) // prints "302"
  println(response.headers(HeaderName.LOCATION).value) // prints "/wiki/"
}
