package examples

import uk.co.bigbeeconsultants.http._
import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.SecureRandom

object Example5a extends App {
  val url = "https://www.amazon.co.uk/"
  val httpClient = new HttpClient
  val response = httpClient.get(url)
  println(response.status)
  println(response.body)
}
