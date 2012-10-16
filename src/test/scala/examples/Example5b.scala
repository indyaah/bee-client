package examples

// http://stackoverflow.com/questions/10135074/download-file-from-https-server-using-java

import uk.co.bigbeeconsultants.http._
import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.SecureRandom

object Example5b extends App {
  // Create a new trust manager that trusts all certificates
  val trustAllCerts = Array[TrustManager](new DumbTrustManager)

  // Activate the new trust manager
  val sc = SSLContext.getInstance("SSL")
  sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())
  HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
  HttpsURLConnection.setDefaultHostnameVerifier(new DumbHostnameVerifier)

  val url = "https://localhost/bee-client/test-echo-back.php"
  val httpClient = new HttpClient
  val response = httpClient.get(url)
  println(response.status)
  println(response.body)
}

/** Don't use this in production code!!! */
class DumbTrustManager extends X509TrustManager {
  def getAcceptedIssuers: Array[X509Certificate] = null
  def checkClientTrusted(certs: Array[X509Certificate], authType: String) {}
  def checkServerTrusted(certs: Array[X509Certificate], authType: String) {}
}

/** Don't use this in production code!!! */
class DumbHostnameVerifier extends HostnameVerifier {
  def verify(p1: String, p2: SSLSession) = true
}
