package examples

import uk.co.bigbeeconsultants.http.{Config, HttpClient}
import uk.co.bigbeeconsultants.http.HttpClient._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header.MediaType
import java.net.URL
import uk.co.bigbeeconsultants.http.header.HeaderName._
import scala.Some

object Example2c extends App {
  val jsonBody = RequestBody(MediaType.APPLICATION_JSON, Map("x" -> "1", "y" -> "2"))
  val url = "http://localhost/lighthttpclient/test-lighthttpclient.php?CT=text/plain"
  val httpClient = new HttpClient(config = Config(followRedirects = false))
  val response = httpClient.post(url, Some(jsonBody))
  println(response.status)
  println(response.headers(LOCATION))
}
