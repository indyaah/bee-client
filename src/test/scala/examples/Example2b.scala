package examples

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.HttpClient._
import uk.co.bigbeeconsultants.http.header.Headers
import uk.co.bigbeeconsultants.http.header.HeaderName._

object Example2b extends App {

  val headers = Headers(
    ACCEPT -> "text/html",
    ACCEPT_LANGUAGE -> "fr"
  )
  val httpClient = new HttpClient
  val response = httpClient.get("http://www.google.com/", headers)

  // prints a list of the response header names available to you
  println(response.headers.names.sorted)

  val startOfBody = response.body.asString.indexOf("<body")
  println(response.body.asString.substring(startOfBody)) // shows some French content
}
