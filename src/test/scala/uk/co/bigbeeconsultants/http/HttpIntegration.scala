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

import header.{HeaderName, Headers, MediaType}
import HttpClient._
import header.HeaderName._
import java.lang.AssertionError
import request.{Config, RequestBody}
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.{ConnectException, Proxy, URL}
import java.io.File

class HttpIntegration extends FunSuite with BeforeAndAfter {

  private val testHtmlFile = "test-lighthttpclient.html"
  private val testTxtFile = "test-lighthttpclient.txt"
  private val testPhpFile = "test-lighthttpclient.php"
  private val test204File = "test-lighthttpclient-204.php"
  private val testImageFile = "B.png"
  private val testPhotoFile = "plataria-sunset.jpg"

  private val serverUrl = "http://localhost/lighthttpclient/"
  private val testHtmlUrl = serverUrl + testHtmlFile
  private val testTxtUrl = serverUrl + testTxtFile
  private val testPhpUrl = serverUrl + testPhpFile
  private val testImageUrl = serverUrl + testImageFile
  private val testPhotoUrl = serverUrl + testPhotoFile

  private val jsonBody = RequestBody (MediaType.APPLICATION_JSON, """{ "x": 1, "y": true }""")
  //  private val proxyAddress = new InetSocketAddress("localhost", 8888);
  //  private val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  private val proxy = Proxy.NO_PROXY

  private val basicHeaders = Headers (ACCEPT_CHARSET -> UTF8)
  private val gzipHeaders = Headers (ACCEPT_ENCODING -> GZIP)

  private val dir = new File ("src/test/resources")
  private val testHtmlSize = 1497
  private val testTxtSize = new File (dir, testTxtFile).length
  private val testImageSize = 497
  private val testPhotoSize = 1605218

  var http: HttpClient = _

  before {
    http = new HttpClient (config = Config (followRedirects = false), commonRequestHeaders = basicHeaders, proxy = proxy)
  }

  after {
    http.closeConnections ()
  }

  test ("setupOK") {
    //    assertFalse(http.config.keepAlive)
  }

  def headTest(url: String, size: Long) {
    try {
      val response = http.head (new URL (url), gzipHeaders)
      expect (200, url)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expectHeaderIfPresent ("gzip")(response.headers, CONTENT_ENCODING)
//      expectHeaderIfPresent (size)(response.headers, CONTENT_LENGTH)
      expect (true)(body.toString == "")
    } catch {
      case e: Exception =>
        skipTestWarning ("HEAD", testPhpUrl, e)
    }
  }

  test ("html text/html head x100") {
    for (i <- 1 to 100) {
      headTest (testHtmlUrl + "?LOREM=" + i, testHtmlSize)
    }
  }

  private def htmlGet(url: String, size: Long) {
    try {
      val response = http.get (new URL (url), gzipHeaders)
      expect (200, url)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      val string = body.toString
      expect (true)(string.startsWith ("<!DOCTYPE html>"))
      expect ("gzip")(response.headers(CONTENT_ENCODING).value)
      //expect (size)(response.headers.get (CONTENT_LENGTH).toInt)
      val bodyLines = string.split ("\n")
      expect ("<!DOCTYPE html>")(bodyLines (0))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testPhpUrl, e)
    }
  }

  test ("html text/html get x100") {
    for (i <- 1 to 100) {
      htmlGet (testHtmlUrl + "?LOREM=" + i, testHtmlSize)
    }
  }

  test ("image/png get x1") {
    try {
      val response = http.get (new URL (testImageUrl), gzipHeaders)
      expect (200)(response.status.code)
      expect (MediaType.IMAGE_PNG)(response.body.contentType)
      val bytes = response.body.asBytes
      expect (testImageSize)(bytes.length)
      expect ('P')(bytes (1))
      expect ('N')(bytes (2))
      expect ('G')(bytes (3))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testImageUrl, e)
    }
  }

  test ("txt text/plain get x1") {
    val url = testTxtUrl + "?CT=text/plain"
    try {
      val response = http.get (new URL (url), gzipHeaders)
      expect (200)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_PLAIN)(body.contentType)
      expect (true)(body.toString.startsWith ("Lorem "))
      val bodyLines = body.toString.split ("\n")
      //expect ("GET")(extractLineFromResponse ("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", url, e)
    }
  }

  test ("php text/plain options x1") {
    val url = testPhpUrl + "?CT=text/plain"
    try {
      val response = http.options (new URL (url), None)
      expect (302)(response.status.code)
      val body = response.body
      expect (0)(body.toString.length)
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", url, e)
    }
  }

  test ("php text/plain post x1") {
    val url = testPhpUrl + "?CT=text/plain"
    try {
      val response = http.post (new URL (url), Some(jsonBody), gzipHeaders)
      expect (302)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expect (0)(body.toString.length)
      val location = response.headers(LOCATION).value
      expect (true, location)(location.startsWith (serverUrl))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testPhpUrl, e)
    }
  }

  test ("php text/html delete x1") {
    try {
      val response = http.delete (new URL (testPhpUrl + "?D=1"), gzipHeaders)
      expect (200)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expect (true)(body.toString.startsWith ("<html>"))
      val bodyLines = body.toString.split ("\n")
      expect ("DELETE")(extractLineFromResponse ("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning ("DELETE", testPhpUrl, e)
    }
  }

  test ("jpg image/jpg get x100") {
    try {
      val loops = 100
      val before = System.currentTimeMillis ()
      for (i <- 1 to loops) {
        val response = http.get (new URL (testPhotoUrl + "?n=" + i), gzipHeaders)
        expect (200)(response.status.code)
        expect (MediaType.IMAGE_JPG)(response.body.contentType)
        val bytes = response.body.asBytes
        expect (testPhotoSize)(bytes.length)
      }
      val duration = System.currentTimeMillis () - before
      val bytes = BigDecimal (testPhotoSize * loops)
      val rate = (bytes / duration)
      println (bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testPhotoUrl, e)
    }
  }

  test ("php text/html get x100") {
    try {
      var size = -1
      var first = "NOT SET"
      val loops = 100
      val before = System.currentTimeMillis ()
      var ok = true
      for (i <- 1 to loops) {
        if (ok) {
          try {
            val is = i.toString
            val response = http.get (new URL (testPhpUrl + "?STUM=1"), gzipHeaders)
            expect (200)(response.status.code)
            val body = response.body
            expect (MediaType.TEXT_HTML)(body.contentType)
            val string = body.toString
            expect (true, is)(string.startsWith ("<html>"))
            if (size < 0) {
              first = string
              size = first.length
            } else {
              expect (first, is)(string)
              expect (size, is)(string.length)
            }
          } catch {
            case e: Exception =>
              skipTestWarning ("GET", "soakTestTextOK " + i, e)
              ok = false
          }
        }
      }

      val duration = System.currentTimeMillis () - before
      val bytes = BigDecimal (size * loops)
      val rate = (bytes / duration)
      println (bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testPhotoUrl, e)
    }
  }

  private def expectHeaderIfPresent(expected: Any)(headers: Headers, name: HeaderName) {
    val hdrs = headers.filter (name.name)
    if (!hdrs.isEmpty) {
      expect (expected)(hdrs (0).value)
    }
  }


  private def extractLineFromResponse(expectedHeader: String, bodyLines: Seq[String]): String = {
    val expectedHeaderColon = expectedHeader + ':'
    for (line <- bodyLines) {
      if (line.startsWith (expectedHeaderColon)) {
        return line.substring (line.indexOf (':') + 1).trim ()
      }
    }
    throw new AssertionError ("Expect response to contain\n" + expectedHeader)
  }

  private def skipTestWarning(method: String, url: String, e: Exception) {
    if (e.isInstanceOf[ConnectException] || e.getCause.isInstanceOf[ConnectException]) {
      System.err.println ("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
    }
    else {
      throw e
    }
  }
}
