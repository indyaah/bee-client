package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response._

object Example5d {
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val request = Request.get("http://www.google.com/")
    val response = httpClient.makeUnbufferedRequest(request)

    val unbufferedBody: ResponseBody = response.body
    // unbufferedBody is an InputStreamResponseBody
    // (only if response.status.code == 200)
    println(unbufferedBody.isBuffered) // false (if no error)

    val bufferedBody: ResponseBody = unbufferedBody.toBufferedBody
    // bufferedBody is a ByteBufferResponseBody
    println(bufferedBody.isBuffered) // true
    println(bufferedBody.contentLength) // now known

    val stringBody: ResponseBody = bufferedBody.toBufferedBody
    // stringBody is a StringResponseBody
    println(stringBody.isBuffered) // true
  }
}
