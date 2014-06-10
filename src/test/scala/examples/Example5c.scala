package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.InputStreamResponseBody

object Example5c {
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val request = Request.get("http://www.bing.com/favicon.ico")
    val response = httpClient.makeUnbufferedRequest(request)

    if (response.body.isBuffered) {
      // usually this means an unsuccessful request
      // ...

    } else {
      val inputStream = response.body.inputStream
      try {
        println(response.body.isBuffered) // false
        println(response.body.isTextual) // false

        // ...use inputStream somewhow...

      } finally {
        inputStream.close() // very important
      }
    }
  }
}
