package examples

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.HttpClient._

object Example1c extends App {

  val httpClient = new HttpClient
  val response = httpClient.get("http://www.google.com/")

  println(response.body.asString.length) // prints a number
  println(response.body.contentLength) // prints the same number
  println(response.body.contentType.value) // prints "text/html"
  println(response.body.contentType.charsetOrUTF8) // prints "ISO-8859-1"
}
