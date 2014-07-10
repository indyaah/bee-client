package examples

import uk.co.bigbeeconsultants.http._

object Example3a {
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val response = httpClient.head("http://www.google.com/")

    println(response.body.contentLength) // prints 0
    println(response.body.asString.length) // the same number
    println(response.body.contentType.mediaType) // prints "text/html"
    println(response.body.contentType.charsetOrUTF8) // prints "ISO-8859-1"
  }
}
