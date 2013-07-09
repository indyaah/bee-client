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
      val unbufferedBody = response.body.asInstanceOf[InputStreamResponseBody]
      val rawStream = unbufferedBody.inputStream
      try {
        println(unbufferedBody.isBuffered) // false
        println(unbufferedBody.isTextual) // false

        // ...use rawStream somewhow...

      } finally {
        rawStream.close() // very important
      }
    }
  }
}
