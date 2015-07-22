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

package uk.co.bigbeeconsultants.http.request

import org.scalatest.FunSuite
import org.mockito.Matchers._
import org.mockito.Mockito._
import java.net.HttpURLConnection
import uk.co.bigbeeconsultants.http.response.Status
import java.io.ByteArrayInputStream
import uk.co.bigbeeconsultants.http.Config
import javax.net.ssl.{HostnameVerifier, SSLSocketFactory, HttpsURLConnection}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PreRequestTest extends FunSuite {

  var httpURLConnection: HttpURLConnection = null

  private def createMock(contentType: String, content: String, status: Status) {
    httpURLConnection = mock(classOf[HttpURLConnection])
    when(httpURLConnection.getContentType).thenReturn(contentType)
    when(httpURLConnection.getResponseCode).thenReturn(status.code)
    when(httpURLConnection.getInputStream).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")))
    when(httpURLConnection.getResponseMessage).thenReturn(status.message)
  }


  test("ConnectionControl with keepAlive = true") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    ConnectionControl.process(null, httpURLConnection, Config(keepAlive = true))
    verify(httpURLConnection, times(0)).setRequestProperty(any(), any())
  }


  test("ConnectionControl with keepAlive = false") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    ConnectionControl.process(null, httpURLConnection, Config(keepAlive = false))
    verify(httpURLConnection, times(1)).setRequestProperty("Connection", "close")
  }


  test("UserAgentString with None") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    UserAgentString.process(null, httpURLConnection, Config(userAgentString = None))
    verify(httpURLConnection, times(0)).setRequestProperty(any(), any())
  }


  test("UserAgentString with some string") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    UserAgentString.process(null, httpURLConnection, Config(userAgentString = Some("Foo bar")))
    verify(httpURLConnection, times(1)).setRequestProperty("User-Agent", "Foo bar")
  }


  test("AutomaticHostHeader with some hostname") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    AutomaticHostHeader.process(Request.get("http://myserver/"), httpURLConnection, Config())
    verify(httpURLConnection, times(1)).setRequestProperty("Host", "myserver")
  }

  test("AutomaticHostHeader with localhost") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    AutomaticHostHeader.process(Request.get("http://localhost/"), httpURLConnection, Config())
    verify(httpURLConnection, times(0)).setRequestProperty(any(), any())
  }

  test("AutomaticHostHeader with IP address") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    AutomaticHostHeader.process(Request.get("http://192.168.1.1/"), httpURLConnection, Config())
    verify(httpURLConnection, times(0)).setRequestProperty(any(), any())
  }


  test("DefaultRequestHeaders with IP address") {
    httpURLConnection = mock(classOf[HttpURLConnection])
    DefaultRequestHeaders.process(null, httpURLConnection, Config())
    verify(httpURLConnection, times(1)).setRequestProperty("Accept", "*/*")
    verify(httpURLConnection, times(1)).setRequestProperty("Accept-Encoding", "gzip")
    verify(httpURLConnection, times(1)).setRequestProperty("Accept-Charset", "UTF-8,*;q=.1")
  }


  test("SSLSocketFactoryInjecter") {
    val httpsURLConnection = mock(classOf[HttpsURLConnection])
    val sslSocketFactory = mock(classOf[SSLSocketFactory])
    val config = Config(preRequests = List(SSLSocketFactoryInjecter), sslSocketFactory = Some(sslSocketFactory))
    SSLSocketFactoryInjecter.process(null, httpsURLConnection, config)
    verify(httpsURLConnection, times(1)).setSSLSocketFactory(sslSocketFactory)
  }


  test("HostnameVerifierInjecter") {
    val httpsURLConnection = mock(classOf[HttpsURLConnection])
    val hostnameVerifier = mock(classOf[HostnameVerifier])
    val config = Config(preRequests = List(SSLSocketFactoryInjecter), hostnameVerifier = Some(hostnameVerifier))
    HostnameVerifierInjecter.process(null, httpsURLConnection, config)
    verify(httpsURLConnection, times(1)).setHostnameVerifier(hostnameVerifier)
  }


}
