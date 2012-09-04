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
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.{HttpURLConnection, Proxy, URL}
import org.mockito.Mockito._
import request.{RequestBody, Request}
import response.Status
import collection.immutable.ListMap
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}


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
      if (v != null) {
        when (httpURLConnection.getHeaderFieldKey (i)).thenReturn (k)
        when (httpURLConnection.getHeaderField (i)).thenReturn (v)
        i += 1
      }
    }
    when (httpURLConnection.getHeaderFieldKey (i)).thenReturn (null)
  }

  private def verifyRequestSettings(method: String, headers: List[Header]) {
    verify (httpURLConnection).setRequestMethod (method)
    for (h <- headers) {
      verify (httpURLConnection).setRequestProperty (h.name, h.value)
    }
  }

  private def verifyHeaders(n: Int) {
    verify (httpURLConnection).getHeaderFieldKey (0)
    for (i <- 1 to n) {
      verify (httpURLConnection).getHeaderFieldKey (i)
      verify (httpURLConnection).getHeaderField (i)
    }
    verify (httpURLConnection).getHeaderFieldKey (n + 1)
  }

  private def verifyConfig(config: Config) {
    verify (httpURLConnection).setConnectTimeout (config.connectTimeout)
    verify (httpURLConnection).setReadTimeout (config.readTimeout)
    verify (httpURLConnection).setInstanceFollowRedirects (config.followRedirects)
    verify (httpURLConnection).setUseCaches (config.useCaches)
    verify (httpURLConnection).setAllowUserInteraction (false)
  }

  private def newHttpClient(config: Config = Config (),
                            commonRequestHeaders: Headers = HttpClient.defaultRequestHeaders) = {
    new HttpClient (config, commonRequestHeaders) {
      override def openConnection(request: Request) = httpURLConnection
    }
  }


  def executeBasicSettingsWithoutBody(method: String, contentType: String, status: Status = Status (200, "OK")) {
    val expectedContent = "hello world"
    createMock (contentType, expectedContent, status)
    expectHeaders (ListMap ("Content-Type" -> contentType))

    val http = newHttpClient ()

    val headers = Headers (List (ACCEPT_LANGUAGE -> "en"))
    val response = method match {
      case "GET" => http.get (url, headers)
      case "HEAD" => http.head (url, headers)
      case "TRACE" => http.trace (url, headers)
      case "DELETE" => http.delete (url, headers)
      case "POST" => http.post (url, Some(RequestBody(MediaType.TEXT_HTML)), headers)
    }

    verifyConfig(Config())
    verifyRequestSettings (method, List(HOST -> "server", ACCEPT -> "*/*",
      ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en"))
    verify (httpURLConnection).getContentType
    verify (httpURLConnection, times (2)).getResponseCode
    verify (httpURLConnection).getResponseMessage
    verify (httpURLConnection).getInputStream
    verify (httpURLConnection).connect ()
    verify (httpURLConnection).disconnect ()
    verifyHeaders (1)
    verifyNoMoreInteractions (httpURLConnection)

    expect (TEXT_PLAIN)(response.body.contentType)
    expect (expectedContent)(response.body.toString)
  }


  test ("mock methods with broadly default settings and without a body should confirm all interations") {
    executeBasicSettingsWithoutBody("GET", "text/plain")
    executeBasicSettingsWithoutBody("HEAD", "text/plain")
    executeBasicSettingsWithoutBody("TRACE", "text/plain")
    executeBasicSettingsWithoutBody("DELETE", "text/plain")
  }


  // TODO
  ignore ("mock methods without a content type and without a body should confirm all interations") {
    val expectedContent = "hello world"
    createMock (null, expectedContent, Status (204, "No content"))
    expectHeaders (ListMap ())

    val http = newHttpClient ()

    val headers = Headers (List (ACCEPT_LANGUAGE -> "en"))
    val response = http.post (url, Some(RequestBody(MediaType.TEXT_HTML)), headers)

    verifyConfig(Config())
    verifyRequestSettings ("POST", List(HOST -> "server", ACCEPT -> "*/*",
      ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en"))
    verify (httpURLConnection).getContentType
    verify (httpURLConnection, times (3)).getResponseCode
    verify (httpURLConnection).getResponseMessage
    verify (httpURLConnection).getInputStream
    verify (httpURLConnection).connect ()
    verify (httpURLConnection).disconnect ()
    verifyHeaders (0)
    verifyNoMoreInteractions (httpURLConnection)

    expect (TEXT_PLAIN)(response.body.contentType)
    expect (expectedContent)(response.body.toString)
  }


  def executeBasicSettingsWithBody(method: String) = {
    val expectedContent = "hello world"
    createMock ("text/plain", expectedContent, Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    val baos = new ByteArrayOutputStream ()
    when (httpURLConnection.getOutputStream).thenReturn (baos)

    val http = newHttpClient ()

    val headers = Headers (List (ACCEPT_LANGUAGE -> "en"))
    val response = method match {
      case "POST" => http.post (url, Some(RequestBody(TEXT_PLAIN, Map("foo" -> "bar", "a" -> "z"))), headers)
      case "PUT" => http.put (url, RequestBody(TEXT_PLAIN, "hello world"), headers)
      case "OPTIONS" => http.options (url, Some(RequestBody(TEXT_PLAIN, "hello world")), headers)    }

    verifyConfig(Config())
    verifyRequestSettings (method, List(HOST -> "server", ACCEPT -> "*/*",
      ACCEPT_ENCODING -> "gzip", ACCEPT_CHARSET -> "UTF-8,*;q=.1", ACCEPT_LANGUAGE -> "en", CONTENT_TYPE -> "text/plain"))
    verify (httpURLConnection).setDoOutput (true)
    verify (httpURLConnection).getContentType
    verify (httpURLConnection, times (2)).getResponseCode
    verify (httpURLConnection).getResponseMessage
    verify (httpURLConnection).getInputStream
    verify (httpURLConnection).getOutputStream
    verify (httpURLConnection).connect ()
    verify (httpURLConnection).disconnect ()
    verifyHeaders (1)
    verifyNoMoreInteractions (httpURLConnection)

    expect (TEXT_PLAIN)(response.body.contentType)
    expect (expectedContent)(response.body.toString)
    baos.toString("UTF-8")
  }


  test ("mock methods with broadly default settings and with a body should confirm all interations") {
    expect("foo=bar&a=z") (executeBasicSettingsWithBody("POST"))
    expect("hello world") (executeBasicSettingsWithBody("PUT"))
    expect("hello world") (executeBasicSettingsWithBody("OPTIONS"))
  }


  test ("config connect timeout should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    val config = Config (connectTimeout = 12345)
    newHttpClient (config).get (url)
    verifyConfig(config)
  }


  test ("config read timeout should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    val config = Config (readTimeout = 12345)
    newHttpClient (config).get (url)
    verifyConfig(config)
  }


  test ("config follow redirects should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    val config = Config (followRedirects = false)
    newHttpClient (config).get (url)
    verifyConfig(config)
  }


  test ("config use caches should send the correct header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    val config = Config (useCaches = false)
    newHttpClient (config).get (url)
    verifyConfig(config)
  }


  test ("config host header flag should be able to disable the host header") {
    createMock ("text/plain", "hello world", Status (200, "OK"))
    expectHeaders (ListMap ("Content-Type" -> "text/plain"))

    newHttpClient (config = Config (sendHostHeader = false)).get (url)

    verify (httpURLConnection, times (0)).setRequestProperty (HOST, "server")
  }

}
