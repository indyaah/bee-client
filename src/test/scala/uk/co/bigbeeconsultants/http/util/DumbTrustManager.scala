//-----------------------------------------------------------------------------
// The MIT License
//
// Copyright (c) 2012 Rick Beton <rick@bigbeeconsultants.co.uk>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//-----------------------------------------------------------------------------

package uk.co.bigbeeconsultants.http.util

import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.SecureRandom

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

object DumbTrustManager {
  val sslSocketFactory = {
    // Create a new trust manager that trust all certificates
    val trustAllCerts = Array[TrustManager](new DumbTrustManager)

    // Activate the new trust manager
    val sc = SSLContext.getInstance("SSL")
    sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())
    sc.getSocketFactory
  }

  val hostnameVerifier = new DumbHostnameVerifier

  def install() {
    HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)
  }
}