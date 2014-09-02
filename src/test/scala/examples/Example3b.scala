package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody

object Example3b {
  val requestBody = RequestBody(Map("x" -> "1", "y" -> "2"))
  val url = "http://httpbin.org/post"
  val httpClient = new HttpClient(Config(connectTimeout = 60000))

  def main(args: Array[String]) {
    val response = httpClient.post(url, Some(requestBody))
    println(response.status)
    println(response.body)
  }
}
