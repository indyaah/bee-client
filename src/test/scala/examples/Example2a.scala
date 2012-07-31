package examples

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.HttpClient._

object Example2a extends App {

  val httpClient = new HttpClient
  val response = httpClient.head("http://www.google.com/")

  println(response.body.asString.length) // prints 0
  println(response.body.contentLength) // prints 0
  println(response.body.contentType.value) // prints "text/html"
  println(response.body.contentType.charsetOrUTF8) // prints "ISO-8859-1"
}
