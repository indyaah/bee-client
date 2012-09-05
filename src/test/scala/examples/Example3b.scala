package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody

object Example3b extends App {
  val requestBody = RequestBody(Map("x" -> "1", "y" -> "2"))
  val url = "http://localhost/lighthttpclient/test-lighthttpclient.php?D=1&CT=text/plain"
  val httpClient = new HttpClient(config = Config(followRedirects = false))
  val response = httpClient.post(url, Some(requestBody))
  println(response.status)
  println(response.body)
}