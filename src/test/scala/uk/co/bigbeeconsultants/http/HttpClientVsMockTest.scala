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

package uk.co.bigbeeconsultants.http

import header._
import HeaderName._
import MediaType._
import request.{Request, Config}
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.{HttpURLConnection, Proxy, URL}
import java.io.ByteArrayInputStream
import org.mockito.Mockito._
import response.{Status, BodyFactory}
import collection.immutable.ListMap


class HttpClientVsMockTest extends FunSuite with BeforeAndAfter {


  val url = new URL ("http://server/some/url")
  var httpURLConnection: HttpURLConnection = null

  private def createMock(contentType: String, content: String, status: Status) {
    httpURLConnection = mock (classOf[HttpURLConnection])
    when (httpURLConnection.getContentType).thenReturn (contentType)
    when (httpURLConnection.getResponseCode).thenReturn (status.code)
    when (httpURLConnection.getInputStream).thenReturn (new ByteArrayInputStream (content.getBytes ("UTF-8")))
    when (httpURLConnection.getResponseMessage).thenReturn (status.message)
  }

  private def expectHeaders(map: ListMap[String, String]) {
    when (httpURLConnection.getHeaderFieldKey (0)).thenReturn (null)
    var i = 1
    for ((k, v) <- map) {
      when (httpURLConnection.getHeaderFieldKey (i)).thenReturn (k)
      when (httpURLConnection.getHeaderField (i)).thenReturn (v)
      i += 1
    }
    when (httpURLConnection.getHeaderFieldKey (i)).thenReturn (null)
  }

  private def verifyHeaders(n: Int) {
    verify (httpURLConnection).getHeaderFieldKey (0)
    for (i <- 1 to n) {
      verify (httpURLConnection).getHeaderFieldKey (i)
      verify (httpURLConnection).getHeaderField (i)
    }
    verify (httpURLConnection).getHeaderFieldKey (n + 1)
  }

  private def newHttpClient(config: Config = Config (),
                            commonRequestHeaders: Headers = HttpClient.defaultRequestHeaders,
                            responseBodyFactory: BodyFactory = HttpClient.defaultResponseBodyFactory,
                            proxy: Proxy = Proxy.NO_PROXY) = {
    new HttpClient (config, commonRequestHeaders, responseBodyFactory, proxy) {
      override def openConnection(request: Request) = httpURLConnection
    }
  }

  test ("mock get with broadly default settings should confirm all interations") {
    val expectedContent = "hello world"
    createMock ("text/plain", expectedContent, Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    val http = newHttpClient ()

    val response = http.get (url)

    verify (httpURLConnection).setAllowUserInteraction (false)
    verify (httpURLConnection).setConnectTimeout (2000)
    verify (httpURLConnection).setReadTimeout (5000);
    verify (httpURLConnection).setInstanceFollowRedirects (true)
    verify (httpURLConnection).setUseCaches (true)
    verify (httpURLConnection).setRequestMethod ("GET")
    verify (httpURLConnection).setRequestProperty (HOST, "server")
    verify (httpURLConnection).setRequestProperty (ACCEPT_ENCODING, "gzip")
    verify (httpURLConnection).setRequestProperty (ACCEPT_CHARSET, "UTF-8")
    verify (httpURLConnection).getContentType
    verify (httpURLConnection, times (3)).getResponseCode
    verify (httpURLConnection).getResponseMessage
    verify (httpURLConnection).getInputStream
    verify (httpURLConnection).connect ()
    verify (httpURLConnection).disconnect ()
    verifyHeaders (1)
    verifyNoMoreInteractions (httpURLConnection)

    expect (TEXT_PLAIN)(response.body.contentType)
    expect (expectedContent)(response.body.toString)
  }

  test ("config connect timeout should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    newHttpClient (config = Config (connectTimeout = 12345)).get (url)

    verify (httpURLConnection).setConnectTimeout (12345)
  }

  test ("config read timeout should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    newHttpClient (config = Config (readTimeout = 12345)).get (url)

    verify (httpURLConnection).setReadTimeout (12345)
  }

  test ("config follow redirects should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    newHttpClient (config = Config (followRedirects = false)).get (url)

    verify (httpURLConnection).setInstanceFollowRedirects (false)
  }

  test ("config use caches should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    newHttpClient (config = Config (useCaches = false)).get (url)

    verify (httpURLConnection).setUseCaches (false)
  }

  test ("config host header flag should be able to disable the host header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    newHttpClient (config = Config (sendHostHeader = false)).get (url)

    verify (httpURLConnection, times (0)).setRequestProperty (HOST, "server")
  }
}
