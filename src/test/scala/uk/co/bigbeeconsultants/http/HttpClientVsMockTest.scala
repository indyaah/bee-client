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
import header.HeaderName._
import MediaType._
import org.scalatest.FunSuite
import java.net.{Proxy, URL}
import javax.net.ssl.{SSLSocketFactory, HostnameVerifier, HttpsURLConnection}
import org.mockito.Mockito._
import request.{RequestBody, Request}
import response.Status
import collection.immutable.ListMap
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}

class HttpClientVsMockTest extends FunSuite {

  val url = new URL("http://server/some/url")

  class TestInputStream(content: String) extends ByteArrayInputStream(content.getBytes("UTF-8")) {
    var closed = false

    override def close() {
      closed = true
    }
  }

  class MockConnection(contentType: String, content: String, status: Status) {
    var consumed = false
    val inputStream = new TestInputStream(content)
    val httpURLConnection = mock(classOf[HttpsURLConnection])
    when(httpURLConnection.getContentType).thenReturn(contentType)
    when(httpURLConnection.getResponseCode).thenReturn(status.code)
    when(httpURLConnection.getInputStream).thenReturn(inputStream)
    when(httpURLConnection.getResponseMessage).thenReturn(status.message)

    def expectHeaders(map: ListMap[String, String]) {
      when(httpURLConnection.getHeaderFieldKey(0)).thenReturn(null)
      var i = 1
      for ((k, v) <- map) {
        if (v != null) {
          when(httpURLConnection.getHeaderFieldKey(i)).thenReturn(k)
          when(httpURLConnection.getHeaderField(i)).thenReturn(v)
          i += 1
        }
      }
      when(httpURLConnection.getHeaderFieldKey(i)).thenReturn(null)
    }

    def verifyRequestSettings(method: String, headers: List[Header]) {
      verify(httpURLConnection).setRequestMethod(method)
      for (h <- headers) {
        verify(httpURLConnection).setRequestProperty(h.name, h.value)
      }
    }

    def verifyHeaders(n: Int) {
      verify(httpURLConnection).getHeaderFieldKey(0)
      for (i <- 1 to n) {
        verify(httpURLConnection).getHeaderFieldKey(i)
        verify(httpURLConnection).getHeaderField(i)
      }
      verify(httpURLConnection).getHeaderFieldKey(n + 1)
    }

    def verifyConfig(config: Config) {
      verify(httpURLConnection).setConnectTimeout(config.connectTimeout)
      verify(httpURLConnection).setReadTimeout(config.readTimeout)
      verify(httpURLConnection).setInstanceFollowRedirects(false)
      verify(httpURLConnection).setUseCaches(config.useCaches)
      verify(httpURLConnection).setAllowUserInteraction(false)
    }
  }

  class Context(contentType: String, content: String, status: Status) {
    val hostnameVerifier = mock(classOf[HostnameVerifier])
    val sslSocketFactory = mock(classOf[SSLSocketFactory])
    val mc1 = new MockConnection(contentType, content, status)
    val mc2 = new MockConnection(contentType, content, status)
    mc2.consumed = true

    def newHttpClient(config: Config) = {
      new HttpClient(config) {
        override def openConnection(request: Request, proxy: Proxy) = {
          if (!mc2.consumed) {
            mc2.consumed = true
            mc2.httpURLConnection
          } else {
            mc1.consumed = true
            mc1.httpURLConnection
          }
        }
      }
    }
  }

  // ----------

  def executeBasicSettingsWithoutRequestBody(method: String, contentType: String, useCookies: Boolean, redirect: Boolean,
                                             status: Status) {
    val expectedContent = "hello world"
    new Context(contentType, expectedContent, status) {
      if (redirect) {
        mc2.consumed = false
        mc2.expectHeaders(ListMap("Content-Type" -> contentType, "Location" -> "http://server/other"))
      }
      mc1.expectHeaders(ListMap("Content-Type" -> contentType))

      val thisUrl = if (redirect) new URL("http://someother/path") else url

      val http = newHttpClient(Config())

      val headers = Headers(List(ACCEPT_LANGUAGE -> "en"))
      val response =
        if (useCookies)
          method match {
            case "GET" => http.get(thisUrl, headers, CookieJar.empty)
            case "HEAD" => http.head(thisUrl, headers, CookieJar.empty)
            case "TRACE" => http.trace(thisUrl, headers, CookieJar.empty)
            case "DELETE" => http.delete(thisUrl, headers, CookieJar.empty)
          }
        else
          method match {
            case "GET" => http.get(thisUrl, headers)
            case "HEAD" => http.head(thisUrl, headers)
            case "TRACE" => http.trace(thisUrl, headers)
            case "DELETE" => http.delete(thisUrl, headers)
          }

      if (redirect) {
        mc2.verifyConfig(Config())
        mc2.verifyRequestSettings(method, List(HOST -> "someother", ACCEPT -> "*/*",
          ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en"))
        verify(mc2.httpURLConnection, atLeastOnce()).getResponseCode
        verify(mc2.httpURLConnection).getResponseMessage
        verify(mc2.httpURLConnection).getInputStream
        verify(mc2.httpURLConnection).connect()
        mc2.verifyHeaders(2)
        verifyNoMoreInteractions(mc2.httpURLConnection)
        assert(mc2.inputStream.closed)
        assert(TEXT_PLAIN === response.body.contentType)
        assert(expectedContent === response.body.asString)
      }

      mc1.verifyConfig(Config())
      mc1.verifyRequestSettings(method, List(HOST -> "server", ACCEPT -> "*/*",
        ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en"))
      verify(mc1.httpURLConnection).getContentType
      verify(mc1.httpURLConnection, atLeastOnce()).getResponseCode
      verify(mc1.httpURLConnection).getResponseMessage
      verify(mc1.httpURLConnection).getInputStream
      verify(mc1.httpURLConnection).connect()
      assert(mc1.inputStream.closed)
      verify(mc1.httpURLConnection).disconnect()
      mc1.verifyHeaders(1)
      verifyNoMoreInteractions(mc1.httpURLConnection)
      assert(TEXT_PLAIN === response.body.contentType)
      assert(expectedContent === response.body.asString)
    }
  }


  test("mock methods with broadly default settings and without a request body should confirm all interactions") {
    executeBasicSettingsWithoutRequestBody("GET", "text/plain", false, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("HEAD", "text/plain", false, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("TRACE", "text/plain", false, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("DELETE", "text/plain", false, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("GET", "text/plain", true, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("HEAD", "text/plain", true, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("TRACE", "text/plain", true, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("DELETE", "text/plain", true, false, Status.S200_OK)
    executeBasicSettingsWithoutRequestBody("GET", "text/plain", false, true, Status.S301_MovedPermanently)
  }

  // ----------

  def executeBasicSettingsWithBufferedBody(method: String, useCookies: Boolean, status: Status) = {
    val expectedContent = "hello world"
    val capturedRequestBody = new ByteArrayOutputStream()

    new Context("text/plain", expectedContent, status) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      when(mc1.httpURLConnection.getOutputStream).thenReturn(capturedRequestBody)

      val http = newHttpClient(Config())

      val headers = Headers(List(ACCEPT_LANGUAGE -> "en"))
      val response =
        if (useCookies)
          method match {
            case "POST" => http.post(url, Some(RequestBody(Map("foo" -> "bar", "a" -> "z"), TEXT_PLAIN)), headers, CookieJar.empty)
            case "PUT" => http.put(url, RequestBody("hello world", TEXT_PLAIN), headers, CookieJar.empty)
            case "OPTIONS" => http.options(url, Some(RequestBody("hello world", TEXT_PLAIN)), headers, CookieJar.empty)
          }
        else
          method match {
            case "POST" => http.post(url, Some(RequestBody(Map("foo" -> "bar", "a" -> "z"), TEXT_PLAIN)), headers)
            case "PUT" => http.put(url, RequestBody("hello world", TEXT_PLAIN), headers)
            case "OPTIONS" => http.options(url, Some(RequestBody("hello world", TEXT_PLAIN)), headers)
          }

      mc1.verifyConfig(Config())
      mc1.verifyRequestSettings(method, List(HOST -> "server", ACCEPT -> "*/*",
        ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en", CONTENT_TYPE -> "text/plain"))
      verify(mc1.httpURLConnection).setDoOutput(true)
      verify(mc1.httpURLConnection).getContentType
      verify(mc1.httpURLConnection, atLeastOnce()).getResponseCode
      verify(mc1.httpURLConnection).getResponseMessage
      verify(mc1.httpURLConnection).getInputStream
      verify(mc1.httpURLConnection).getOutputStream
      verify(mc1.httpURLConnection).connect()
      assert(mc1.inputStream.closed)
      verify(mc1.httpURLConnection).disconnect()
      mc1.verifyHeaders(1)
      verifyNoMoreInteractions(mc1.httpURLConnection)

      assert(TEXT_PLAIN === response.body.contentType)
      assert(expectedContent === response.body.asString)
    }

    capturedRequestBody.toString("UTF-8")
  }


  test("mock methods with broadly default settings and with a request body and a buffered response body should confirm all interactions") {
    assert("foo=bar&a=z" === executeBasicSettingsWithBufferedBody("POST", false, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithBufferedBody("PUT", false, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithBufferedBody("OPTIONS", false, Status.S200_OK))
    assert("foo=bar&a=z" === executeBasicSettingsWithBufferedBody("POST", true, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithBufferedBody("PUT", true, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithBufferedBody("OPTIONS", true, Status.S200_OK))
    assert("foo=bar&a=z" === executeBasicSettingsWithBufferedBody("POST", false, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithBufferedBody("PUT", false, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithBufferedBody("OPTIONS", false, Status.S204_NoContent))
    assert("foo=bar&a=z" === executeBasicSettingsWithBufferedBody("POST", true, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithBufferedBody("PUT", true, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithBufferedBody("OPTIONS", true, Status.S204_NoContent))
  }

  // ----------

  def executeBasicSettingsWithUnbufferedBody(method: String, useCookies: Boolean, status: Status) = {
    val expectedContent = "hello world"
    val capturedRequestBody = new ByteArrayOutputStream()

    new Context("text/plain", expectedContent, status) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      when(mc1.httpURLConnection.getOutputStream).thenReturn(capturedRequestBody)

      val http = newHttpClient(Config())

      val headers = Headers(List(ACCEPT_LANGUAGE -> "en"))
      val response =
        if (useCookies)
          method match {
            case "POST" => http.makeUnbufferedRequest(Request.post(url, Some(RequestBody(Map("foo" -> "bar", "a" -> "z"), TEXT_PLAIN)), headers, Some(CookieJar.empty)))
            case "PUT" => http.makeUnbufferedRequest(Request.put(url, RequestBody("hello world", TEXT_PLAIN), headers, Some(CookieJar.empty)))
            case "OPTIONS" => http.makeUnbufferedRequest(Request.options(url, Some(RequestBody("hello world", TEXT_PLAIN)), headers, Some(CookieJar.empty)))
          }
        else
          method match {
            case "POST" => http.makeUnbufferedRequest(Request.post(url, Some(RequestBody(Map("foo" -> "bar", "a" -> "z"), TEXT_PLAIN)), headers))
            case "PUT" => http.makeUnbufferedRequest(Request.put(url, RequestBody("hello world", TEXT_PLAIN), headers))
            case "OPTIONS" => http.makeUnbufferedRequest(Request.options(url, Some(RequestBody("hello world", TEXT_PLAIN)), headers))
          }
      response.body.toBufferedBody

      mc1.verifyConfig(Config())
      mc1.verifyRequestSettings(method, List(HOST -> "server", ACCEPT -> "*/*",
        ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en", CONTENT_TYPE -> "text/plain"))
      verify(mc1.httpURLConnection).setDoOutput(true)
      verify(mc1.httpURLConnection).getContentType
      verify(mc1.httpURLConnection, atLeastOnce()).getResponseCode
      verify(mc1.httpURLConnection).getResponseMessage
      verify(mc1.httpURLConnection).getInputStream
      verify(mc1.httpURLConnection).getOutputStream
      verify(mc1.httpURLConnection).connect()
      assert(mc1.inputStream.closed)
      verify(mc1.httpURLConnection).disconnect()
      mc1.verifyHeaders(1)
      verifyNoMoreInteractions(mc1.httpURLConnection)

      assert(TEXT_PLAIN === response.body.contentType)
      assert(expectedContent === response.body.toBufferedBody.asString)
    }

    capturedRequestBody.toString("UTF-8")
  }


  test("mock methods with broadly default settings and with a request body and an unbuffered response body should confirm all interactions") {
    assert("foo=bar&a=z" === executeBasicSettingsWithUnbufferedBody("POST", false, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("PUT", false, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("OPTIONS", false, Status.S200_OK))
    assert("foo=bar&a=z" === executeBasicSettingsWithUnbufferedBody("POST", true, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("PUT", true, Status.S200_OK))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("OPTIONS", true, Status.S200_OK))
    assert("foo=bar&a=z" === executeBasicSettingsWithUnbufferedBody("POST", false, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("PUT", false, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("OPTIONS", false, Status.S204_NoContent))
    assert("foo=bar&a=z" === executeBasicSettingsWithUnbufferedBody("POST", true, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("PUT", true, Status.S204_NoContent))
    assert("hello world" === executeBasicSettingsWithUnbufferedBody("OPTIONS", true, Status.S204_NoContent))
  }

  // ----------

  test("config connect timeout should send the correct header") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(connectTimeout = 12345)
      newHttpClient(config).get(url)
      mc1.verifyConfig(config)
    }
  }


  test("config read timeout should send the correct header") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(readTimeout = 12345)
      newHttpClient(config).get(url)
      mc1.verifyConfig(config)
    }
  }


  test("config follow redirects should send the correct header") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(followRedirects = false)
      newHttpClient(config).get(url)
      mc1.verifyConfig(config)
    }
  }


  test("config use caches should send the correct header") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(useCaches = false)
      newHttpClient(config).get(url)
      mc1.verifyConfig(config)
    }
  }


  test("config hostnameVerifier should be set up correctly") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(hostnameVerifier = Some(hostnameVerifier))
      newHttpClient(config).get(url)
      verify(mc1.httpURLConnection).setHostnameVerifier(hostnameVerifier)
    }
  }


  test("config sslSocketFactory should be set up correctly") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(sslSocketFactory = Some(sslSocketFactory))
      newHttpClient(config).get(url)
      verify(mc1.httpURLConnection).setSSLSocketFactory(sslSocketFactory)
    }
  }


  test("config host header flag should be able to disable the host header") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      newHttpClient(config = Config(preRequests = Nil)).get(url)

      verify(mc1.httpURLConnection, times(0)).setRequestProperty(HOST, "server")
    }
  }


  test("config host header should not be automatically sent for IP addresses") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val url = new URL("http://192.168.1.1/some/url")
      newHttpClient(config = Config()).get(url)

      verify(mc1.httpURLConnection, times(0)).setRequestProperty(HOST, "192.168.1.1")
    }
  }


  test("config host header should not be automatically sent for localhost") {
    new Context("text/plain", "hello world", Status.S200_OK) {
      mc1.expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val url = new URL("http://localhost/some/url")
      newHttpClient(config = Config()).get(url)

      verify(mc1.httpURLConnection, times(0)).setRequestProperty(HOST, "localhost")
    }
  }
}
