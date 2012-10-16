package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody

object Example3b extends App {
  val requestBody = RequestBody(Map("x" -> "1", "y" -> "2"))
  val url = "http://beeclient/test-echo-back.php"
  val httpClient = new HttpClient
  val response = httpClient.post(url, Some(requestBody))
  println(response.status)
  println(response.body)
}
