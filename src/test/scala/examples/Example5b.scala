package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request

object Example5b {
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val request = Request.get("http://www.google.com/")
    val response = httpClient.makeUnbufferedRequest(request)

    val unbufferedBody = response.body
    println(unbufferedBody.isBuffered) // false
    println(unbufferedBody.isTextual) // true

    // note that unbufferedBody.contentLength is not available

    for (line <- unbufferedBody) {
      println(line)
    }

    unbufferedBody.close() // would already have been closed in this case
  }
}
