package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.header.HeaderName._

object Example2d extends App {
  val jsonBody = RequestBody(MediaType.APPLICATION_JSON, """{ "x": 1, "y": true }""")
  val url = "http://localhost/lighthttpclient/test-lighthttpclient.php?CT=text/plain"
  val httpClient = new HttpClient(config = Config(followRedirects = false))
  val response = httpClient.put(url, jsonBody)
  println(response.status)
  println(response.headers(LOCATION))
}
