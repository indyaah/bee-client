package examples

// http://stackoverflow.com/questions/10135074/download-file-from-https-server-using-java

import uk.co.bigbeeconsultants.http._
import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.SecureRandom

object Example7b extends App {
  val config = Config().allowInsecureSSL
  val url = "https://localhost/bee-client/test-echo-back.php"
  val httpClient = new HttpClient(config)
  val response = httpClient.get(url)
  println(response.status)
  println(response.body)
}
