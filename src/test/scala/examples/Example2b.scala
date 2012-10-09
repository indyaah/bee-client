package examples

import uk.co.bigbeeconsultants.http._
import request.Request
import header.Headers
import header.HeaderName._

object Example2b extends App {

  val headers = Headers(
    ACCEPT -> "text/html",
    ACCEPT_LANGUAGE -> "fr"
  )
  val httpClient = new HttpClient
  val request = Request.get("http://www.google.com/")
  val response = httpClient.makeRequest(request + headers)

  // prints a list of the response header names available to you
  println(response.headers.names.sorted)

  val startOfBody = response.body.asString.indexOf("<body")
  println(response.body.asString.substring(startOfBody)) // shows some French content
}
