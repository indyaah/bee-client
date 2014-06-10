package examples

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.MediaType._

object Example3c {
  val jsonBody = RequestBody( """{ "x": 1, "y": true }""", APPLICATION_JSON)
  val url = "http://beeclient/test-echo-back.php"
  val httpClient = new HttpClient

  def main(args: Array[String]) {
    val response = httpClient.put(url, jsonBody)
    println(response.status)
    println(response.body)
  }
}
