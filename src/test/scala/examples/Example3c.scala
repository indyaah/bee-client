package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.MediaType._

object Example3c extends App {
  val jsonBody = RequestBody("""{ "x": 1, "y": true }""", APPLICATION_JSON)
  val url = "http://localhost/lighthttpclient/test-lighthttpclient.php?D=1&CT=text/plain"
  val httpClient = new HttpClient(config = Config(followRedirects = false))
  val response = httpClient.put(url, jsonBody)
  println(response.status)
  println(response.body)
}
