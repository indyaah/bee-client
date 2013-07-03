package examples

import uk.co.bigbeeconsultants.http._

object Example1c {
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val response = httpClient.get("http://www.google.com/")

    println(response.body.contentLength)
    println(response.body.asBytes.length) // the same as above
    println(response.body.asString.length)
    println(response.body.isTextual) // true
    println(response.body.contentType.value) // prints "text/html"
    println(response.body.contentType.charsetOrUTF8) // prints "ISO-8859-1"
  }
}
