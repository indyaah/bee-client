package examples

import uk.co.bigbeeconsultants.http._
import request.Request
import header.Headers
import header.HeaderName._

object Example2b {
  val headers = Headers(
    ACCEPT -> "text/html",
    ACCEPT_LANGUAGE -> "fr"
  )
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val request = Request.get("http://www.google.com/")
    val response = httpClient.makeRequest(request + headers)

    // prints a list of the response header names available to you
    println(response.headers.names.sorted)

    val startOfBody = response.body.asString.indexOf("<body")
    // shows some French content
    println(response.body.asString.substring(startOfBody))
  }
}
