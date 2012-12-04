package examples

import uk.co.bigbeeconsultants.http._
import javax.net.ssl._
import java.security.SecureRandom
import uk.co.bigbeeconsultants.http.{Config, HttpClient}

object Example5c extends App {
  // Create a new trust manager that trusts all certificates
  val trustAllCerts = Array[TrustManager](new DumbTrustManager)

  // Activate the new trust manager
  val sc = SSLContext.getInstance("SSL")
  sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())

  val url = "https://localhost/bee-client/test-echo-back.php"
  val config = Config(sslSocketFactory = Some(sc.getSocketFactory), hostnameVerifier = Some(new DumbHostnameVerifier))
  val httpClient = new HttpClient(config)
  val response = httpClient.get(url)
  println(response.status)
  println(response.body)
}

///** Don't use this in production code!!! */
//class DumbTrustManager extends X509TrustManager {
//  def getAcceptedIssuers: Array[X509Certificate] = null
//  def checkClientTrusted(certs: Array[X509Certificate], authType: String) {}
//  def checkServerTrusted(certs: Array[X509Certificate], authType: String) {}
//}
//
///** Don't use this in production code!!! */
//class DumbHostnameVerifier extends HostnameVerifier {
//  def verify(p1: String, p2: SSLSession) = true
//}
