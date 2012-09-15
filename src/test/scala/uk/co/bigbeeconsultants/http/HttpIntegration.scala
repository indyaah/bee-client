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

import HttpClient._
import header._
import header.MediaType._
import header.HeaderName._
import java.lang.AssertionError
import request.RequestBody
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.{ConnectException, Proxy, URL}
import java.io.File
import scala.Some

object HttpIntegration {

  val testHtmlFile = "test-lighthttpclient.html"
  val testTxtFile = "test-lighthttpclient.txt"
  val testPhpFile = "test-lighthttpclient.php"
  val testEchoFile = "test-echo-back.php"
  val test204File = "test-204-no-content.php"
  val test404File = "test-404-with-content.php"
  val testRedirectFile = "test-redirect.php"
  val testCookieFile = "test-setcookie.php"
  val testImageFile = "B.png"
  val testPhotoFile = "plataria-sunset.jpg"

  val serverUrl = "http://localhost/lighthttpclient/"

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Proxy.NO_PROXY

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)

  val dir = new File("src/test/resources")
  val testHtmlSize = 1497
  val testTxtSize = new File(dir, testTxtFile).length
  val testImageSize = 497
  val testPhotoSize = 1605218

  def headTest(http: HttpClient, url: String, size: Long) {
    try {
      val response = http.head(new URL(url), gzipHeaders)
      assert(response.status.code == 200, url)
      val body = response.body
      assert(body.contentType == TEXT_HTML)
      expectHeaderIfPresent("gzip")(response.headers, CONTENT_ENCODING)
      //      expectHeaderIfPresent (size)(response.headers, CONTENT_LENGTH)
      assert(body.toString == "")
    } catch {
      case e: Exception =>
        skipTestWarning("HEAD", url, e)
    }
  }

  private def htmlGet(http: HttpClient, url: String, size: Long) {
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code == 200, url)
      val body = response.body
      assert(body.contentType == TEXT_HTML)
      val string = body.toString
      assert(string startsWith "<!DOCTYPE html>")
      assert(response.headers(CONTENT_ENCODING).value == "gzip", response.headers(CONTENT_ENCODING))
      //expect (size)(response.headers.get (CONTENT_LENGTH).toInt)
      val bodyLines = string.split("\n")
      assert(bodyLines(0) startsWith "<!DOCTYPE html>")
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  private def expectHeaderIfPresent(expected: Any)(headers: Headers, name: HeaderName) {
    val hdrs = headers filter name
    if (!hdrs.isEmpty) {
      assert(hdrs(0).value == expected, hdrs(0))
    }
  }

  private def extractLineFromResponse(expectedHeader: String, bodyLines: Seq[String]): String = {
    val expectedHeaderColon = expectedHeader + ':'
    for (line <- bodyLines) {
      if (line.startsWith(expectedHeaderColon)) {
        return line.substring(line.indexOf(':') + 1).trim()
      }
    }
    throw new AssertionError("Expect response to contain\n" + expectedHeader)
  }

  private def skipTestWarning(method: String, url: String, e: Exception) {
    if (e.isInstanceOf[ConnectException] || e.getCause.isInstanceOf[ConnectException]) {
      System.err.println("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
    }
    else {
      throw e
    }
  }
}


class HttpIntegration extends FunSuite with BeforeAndAfter {

  import HttpIntegration._

  private val jsonSample = """{ "x": 1, "y": true }"""
  private val jsonBody = RequestBody(jsonSample, APPLICATION_JSON)

  val config = Config(followRedirects = false, proxy = proxy)
  var http: HttpClient = _

  before {
    http = new HttpClient(config)
  }

  test("setupOK") {
    //    assertFalse(http.config.keepAlive)
  }

  test("html text/html head x100") {
    for (i <- 1 to 100) {
      headTest(http, serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
    }
  }

  test("html text/html get x100") {
    for (i <- 1 to 100) {
      htmlGet(http, serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
    }
  }

  test("html text/html get giving 204 x100") {
    val url = serverUrl + test204File
    try {
      val response = http.get(new URL(url), gzipHeaders)
      expect(204, url)(response.status.code)
      val body = response.body
      expect(false, response.status)(response.status.isBodyAllowed)
      expect(TEXT_HTML)(body.contentType)
      expect("")(body.toString)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("image/png get x1") {
    val url = serverUrl + testImageFile
    try {
      val response = http.get(new URL(url), gzipHeaders)
      expect(200)(response.status.code)
      expect(IMAGE_PNG)(response.body.contentType)
      val bytes = response.body.asBytes
      expect(testImageSize)(bytes.length)
      expect('P')(bytes(1))
      expect('N')(bytes(2))
      expect('G')(bytes(3))
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get x1") {
    val url = serverUrl + testTxtFile
    try {
      val response = http.get(new URL(url), gzipHeaders)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      expect(true)(body.toString.startsWith("Lorem "))
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get following redirect") {
    val url = serverUrl + testRedirectFile
    try {
      val cookie = Cookie("c1", "v1", Domain.localhost)
      val http2 = new HttpClient
      val response = http2.get(new URL(url), gzipHeaders, CookieJar(cookie))
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      expect(true)(body.toString.startsWith("Lorem "))
      expect(cookie)(response.cookies.get.find(_.name == "c1").get)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get qith query string x1") {
    val url = serverUrl + testEchoFile + "?A=1&B=2"
    try {
      val response = http.get(new URL(url), gzipHeaders)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      val bodyLines = response.body.toString.split("\n").toSeq
      expect("", response.body)(extractLineFromResponse("CONTENT_LENGTH", bodyLines))
      expect("", response.body)(extractLineFromResponse("CONTENT_TYPE", bodyLines))
      expect(Set("A: 1", "B: 2"), response.body)(bodyLines.filter(_.startsWith("GET:")).map(_.substring(5)).toSet)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get acquiring a cookie") {
    val url = serverUrl + testCookieFile
    try {
      val response = http.get(new URL(url), gzipHeaders, CookieJar.empty)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      val cookies = response.cookies.get
      expect(1)(cookies.size)
      expect("v1")(cookies.get("c1").get.value)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/plain options x1") {
    val url = serverUrl + testPhpFile + "?CT=text/plain"
    try {
      val response = http.options(new URL(url), None)
      expect(302)(response.status.code)
      val body = response.body
      expect(0)(body.toString.length)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/plain post x1") {
    val url = serverUrl + testEchoFile
    try {
      val response = http.post(new URL(url), Some(jsonBody), gzipHeaders)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      val bodyLines = response.body.toString.split("\n").toSeq
      expect(jsonSample.length.toString)(extractLineFromResponse("CONTENT_LENGTH", bodyLines))
      expect(APPLICATION_JSON.value)(extractLineFromResponse("CONTENT_TYPE", bodyLines))
      expect(jsonSample)(extractLineFromResponse("PUT", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("POST", url, e)
    }
  }

  test("php text/plain put x1") {
    val url = serverUrl + testEchoFile
    try {
      val response = http.put(new URL(url), jsonBody, gzipHeaders)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      val bodyLines = response.body.toString.split("\n").toSeq
      expect(jsonSample.length.toString)(extractLineFromResponse("CONTENT_LENGTH", bodyLines))
      expect(APPLICATION_JSON.value)(extractLineFromResponse("CONTENT_TYPE", bodyLines))
      expect(jsonSample)(extractLineFromResponse("PUT", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("PUT", url, e)
    }
  }

  test("php text/html delete x1") {
    val url = serverUrl + testEchoFile
    try {
      val response = http.delete(new URL(url), gzipHeaders)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      val bodyLines = response.body.toString.split("\n").toSeq
      expect("DELETE")(extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("DELETE", url, e)
    }
  }

  test("php text/html options x1") {
    val url = serverUrl + testEchoFile
    try {
      val response = http.options(new URL(url), None)
      expect(200)(response.status.code)
      val body = response.body
      expect(TEXT_PLAIN)(body.contentType)
      val bodyLines = response.body.toString.split("\n").toSeq
      expect("OPTIONS")(extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("DELETE", url, e)
    }
  }

  test("jpg image/jpg get x100") {
    val url = serverUrl + testPhotoFile
    try {
      val loops = 100
      val before = System.currentTimeMillis()
      for (i <- 1 to loops) {
        val response = http.get(new URL(url + "?n=" + i), gzipHeaders)
        expect(200)(response.status.code)
        expect(IMAGE_JPG)(response.body.contentType)
        val bytes = response.body.asBytes
        expect(testPhotoSize)(bytes.length)
      }
      val duration = System.currentTimeMillis() - before
      val bytes = BigDecimal(testPhotoSize * loops)
      val rate = (bytes / duration)
      println(bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/html get x100") {
    val url = serverUrl + testPhpFile
    try {
      var size = -1
      var first = "NOT SET"
      val loops = 100
      val before = System.currentTimeMillis()
      var ok = true
      for (i <- 1 to loops) {
        if (ok) {
          try {
            val is = i.toString
            val response = http.get(new URL(url + "?STUM=1"), gzipHeaders)
            expect(200)(response.status.code)
            val body = response.body
            expect(TEXT_HTML)(body.contentType)
            val string = body.toString
            expect(true, is)(string.startsWith("<html>"))
            if (size < 0) {
              first = string
              size = first.length
            } else {
              expect(first, is)(string)
              expect(size, is)(string.length)
            }
          } catch {
            case e: Exception =>
              skipTestWarning("GET", "soakTestTextOK " + i, e)
              ok = false
          }
        }
      }

      val duration = System.currentTimeMillis() - before
      val bytes = BigDecimal(size * loops)
      val rate = if (duration > 0) (bytes / duration) else 0
      println(bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }
}
