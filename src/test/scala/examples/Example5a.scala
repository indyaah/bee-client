package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.response.ResponseBody
import uk.co.bigbeeconsultants.http.request.Request

object Example5a {
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    // terse way
    // val response = httpClient.get("http://www.google.com/")

    // more explicit way
    val request = Request.get("http://www.google.com/")
    val response = httpClient.makeRequest(request)

    val body: ResponseBody = response.body
    println(body.isBuffered) // true
    println(body.isTextual) // true
    println(body.contentLength) // a number
    println(body.asBytes.length) // the same number
    println(body.asString.length) // a similar number
    println(body.contentType.value) // "text/html"
    println(body.contentType.charsetOrUTF8) // "ISO8859-1" or similar
  }
}
