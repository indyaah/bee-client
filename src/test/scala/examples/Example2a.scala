package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.header.Headers
import uk.co.bigbeeconsultants.http.header.HeaderName._

object Example2a {

  val headers = Headers(
    ACCEPT -> "text/html",
    ACCEPT_LANGUAGE -> "fr"
  )
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val response = httpClient.get("http://www.google.com/", headers)

    // prints a list of the response header names available to you
    println(response.headers.names.sorted)

    val startOfBody = response.body.asString.indexOf("<body")
    // show some French content
    println(response.body.asString.substring(startOfBody))
  }
}
