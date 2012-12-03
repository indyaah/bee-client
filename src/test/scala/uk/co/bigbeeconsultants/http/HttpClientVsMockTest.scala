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

  class Context(contentType: String, content: String, status: Status) {
    val hostnameVerifier = mock(classOf[HostnameVerifier])
    val sslSocketFactory = mock(classOf[SSLSocketFactory])

    val httpURLConnection = mock(classOf[HttpsURLConnection])
    when(httpURLConnection.getContentType).thenReturn(contentType)
    when(httpURLConnection.getResponseCode).thenReturn(status.code)
    when(httpURLConnection.getInputStream).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")))
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

    def newHttpClient(config: Config) = {
      new HttpClient(config) {
        override def openConnection(request: Request, proxy: Proxy) = httpURLConnection
      }
    }
  }

  // ----------

  def executeBasicSettingsWithoutBody(method: String, contentType: String, useCookies: Boolean, status: Status = Status(200, "OK")) {
    val expectedContent = "hello world"
    new Context(contentType, expectedContent, status) {
      expectHeaders(ListMap("Content-Type" -> contentType))

      val http = newHttpClient(Config())

      val headers = Headers(List(ACCEPT_LANGUAGE -> "en"))
      val response =
        if (useCookies)
          method match {
            case "GET" => http.get(url, headers, CookieJar.empty)
            case "HEAD" => http.head(url, headers, CookieJar.empty)
            case "TRACE" => http.trace(url, headers, CookieJar.empty)
            case "DELETE" => http.delete(url, headers, CookieJar.empty)
          }
        else
          method match {
            case "GET" => http.get(url, headers)
            case "HEAD" => http.head(url, headers)
            case "TRACE" => http.trace(url, headers)
            case "DELETE" => http.delete(url, headers)
          }

      verifyConfig(Config())
      verifyRequestSettings(method, List(HOST -> "server", ACCEPT -> "*/*",
        ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en"))
      verify(httpURLConnection).getContentType
      verify(httpURLConnection, times(2)).getResponseCode
      verify(httpURLConnection).getResponseMessage
      verify(httpURLConnection).getInputStream
      verify(httpURLConnection).connect()
      verify(httpURLConnection).disconnect()
      verifyHeaders(1)
      verifyNoMoreInteractions(httpURLConnection)

      assert(TEXT_PLAIN === response.body.contentType)
      assert(expectedContent === response.body.toString)
    }
  }


  test("mock methods with broadly default settings and without a body should confirm all interations") {
    executeBasicSettingsWithoutBody("GET", "text/plain", false)
    executeBasicSettingsWithoutBody("HEAD", "text/plain", false)
    executeBasicSettingsWithoutBody("TRACE", "text/plain", false)
    executeBasicSettingsWithoutBody("DELETE", "text/plain", false)
    executeBasicSettingsWithoutBody("GET", "text/plain", true)
    executeBasicSettingsWithoutBody("HEAD", "text/plain", true)
    executeBasicSettingsWithoutBody("TRACE", "text/plain", true)
    executeBasicSettingsWithoutBody("DELETE", "text/plain", true)
  }


  def executeBasicSettingsWithBody(method: String, useCookies: Boolean) = {
    val expectedContent = "hello world"
    val baos = new ByteArrayOutputStream()

    new Context("text/plain", expectedContent, Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      when(httpURLConnection.getOutputStream).thenReturn(baos)

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

      verifyConfig(Config())
      verifyRequestSettings(method, List(HOST -> "server", ACCEPT -> "*/*",
        ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en", CONTENT_TYPE -> "text/plain"))
      verify(httpURLConnection).setDoOutput(true)
      verify(httpURLConnection).getContentType
      verify(httpURLConnection, times(2)).getResponseCode
      verify(httpURLConnection).getResponseMessage
      verify(httpURLConnection).getInputStream
      verify(httpURLConnection).getOutputStream
      verify(httpURLConnection).connect()
      verify(httpURLConnection).disconnect()
      verifyHeaders(1)
      verifyNoMoreInteractions(httpURLConnection)

      assert(TEXT_PLAIN === response.body.contentType)
      assert(expectedContent === response.body.toString)
    }

    baos.toString("UTF-8")
  }


  test("mock methods with broadly default settings and with a body should confirm all interations") {
    assert("foo=bar&a=z" === executeBasicSettingsWithBody("POST", false))
    assert("hello world" === executeBasicSettingsWithBody("PUT", false))
    assert("hello world" === executeBasicSettingsWithBody("OPTIONS", false))
    assert("foo=bar&a=z" === executeBasicSettingsWithBody("POST", true))
    assert("hello world" === executeBasicSettingsWithBody("PUT", true))
    assert("hello world" === executeBasicSettingsWithBody("OPTIONS", true))
  }


  test("config connect timeout should send the correct header") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(connectTimeout = 12345)
      newHttpClient(config).get(url)
      verifyConfig(config)
    }
  }


  test("config read timeout should send the correct header") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(readTimeout = 12345)
      newHttpClient(config).get(url)
      verifyConfig(config)
    }
  }


  test("config follow redirects should send the correct header") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(followRedirects = false)
      newHttpClient(config).get(url)
      verifyConfig(config)
    }
  }


  test("config use caches should send the correct header") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(useCaches = false)
      newHttpClient(config).get(url)
      verifyConfig(config)
    }
  }


  test("config hostnameVerifier should be set up correctly") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(hostnameVerifier = Some(hostnameVerifier))
      newHttpClient(config).get(url)
      verify(httpURLConnection).setHostnameVerifier(hostnameVerifier)
    }
  }


  test("config sslSocketFactory should be set up correctly") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val config = Config(sslSocketFactory = Some(sslSocketFactory))
      newHttpClient(config).get(url)
      verify(httpURLConnection).setSSLSocketFactory(sslSocketFactory)
    }
  }


  test("config host header flag should be able to disable the host header") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      newHttpClient(config = Config(preRequests = Nil)).get(url)

      verify(httpURLConnection, times(0)).setRequestProperty(HOST, "server")
    }
  }


  test("config host header should not be automatically sent for IP addresses") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val url = new URL("http://192.168.1.1/some/url")
      newHttpClient(config = Config()).get(url)

      verify(httpURLConnection, times(0)).setRequestProperty(HOST, "192.168.1.1")
    }
  }


  test("config host header should not be automatically sent for localhost") {
    new Context("text/plain", "hello world", Status(200, "OK")) {
      expectHeaders(ListMap("Content-Type" -> "text/plain"))

      val url = new URL("http://localhost/some/url")
      newHttpClient(config = Config()).get(url)

      verify(httpURLConnection, times(0)).setRequestProperty(HOST, "localhost")
    }
  }
}
