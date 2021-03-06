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

import java.io.File
import java.net.{ConnectException, Proxy, URL, UnknownHostException}

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import uk.co.bigbeeconsultants.http.HttpClient._
import uk.co.bigbeeconsultants.http.auth.{Credential, CredentialSuite}
import uk.co.bigbeeconsultants.http.cache.CacheConfig
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.{HeaderName, Headers, _}
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.url.Domain
import uk.co.bigbeeconsultants.http.util.{DiagnosticTimer, Duration}

// scala actors futures retain 2.9.1 backward compatibility
import scala.actors._

object HttpIntegration {
  val testHtmlFile = "test-lighthttpclient.html"
  val testTxtFile = "lorem1.txt"
  val testPhpFile = "test-lighthttpclient.php"
  val testEchoFile = "test-echo-back.php"
  val test204File = "test-204-no-content.php"
  val test404File = "test-404-with-content.php"
  val testRedirect1File = "test-redirect1.php"
  val testRedirect2File = "test-redirect2.php"
  val testCookieFile = "test-setcookie.php"
  val testImageFile = "B.png"
  val testPhotoFile = "plataria-sunset.jpg"

  val serverUrl = "//beeclient/"

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Some(Proxy.NO_PROXY)

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)

  val dir = new File("src/test/resources")
  val testHtmlSize = 1497
  val testTxtSize = new File(dir, testTxtFile).length
  val testImageSize = 497
  val testPhotoSize = 1605218

  private val jsonSample = """{ "x": 1, "y": true }"""
  private val jsonBody = RequestBody(jsonSample, APPLICATION_JSON)

  val configNoRedirects = Config(connectTimeout = 20000, followRedirects = false, proxy = proxy).allowInsecureSSL

  /** Provides a single-threaded soak-tester. */
  def main(args: Array[String]) {
    val concurrency = 9
    val futures = for (i <- 1 to concurrency) yield {
      Futures.future {
        instance("t" + i, 100)
      }
    }
    val totals = Futures.awaitAll(10000000, futures: _*).map(_.get).map(_.asInstanceOf[Duration])
    println("Grand total " + totals.foldLeft(Duration.Zero)(_ + _))
    //Thread.sleep(60000) // time for inspecting any lingering network connections
  }

  def instance(id: String, n: Int) = {
    val h = new HttpIntegration
    val hc = new HttpClient(configNoRedirects)
    val bigbee = new Credential("bigbee", "HelloWorld")
    val cacheConfig = CacheConfig(enabled = true)
    val hb = new HttpBrowser(configNoRedirects, CookieJar.Empty, new CredentialSuite(Map("Restricted" -> bigbee)), cacheConfig)

    var total = Duration(0L)
    for (i <- 1 to n) {
      //println(id + ": iteration " + i)
      val dt = new DiagnosticTimer
      h.headTest(hc, "http:" + serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
      h.headTest(hc, "https:" + serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)

      h.htmlGet(hc, "http:" + serverUrl + testHtmlFile + "?LOREM=" + i, GZIP, testHtmlSize)
      h.htmlGet(hc, "https:" + serverUrl + testHtmlFile + "?LOREM=" + i, GZIP, testHtmlSize)

      h.textHtmlGet204("http:" + serverUrl + test204File)

      h.imagePngGet("http:" + serverUrl + testImageFile)
      h.imagePngGet("https:" + serverUrl + testImageFile)

      h.textPlainGet("http:" + serverUrl + testTxtFile)
      h.textPlainGet("https:" + serverUrl + testTxtFile)

      h.textPlainGetFollowingRedirect("http:" + serverUrl + testRedirect1File + "?TO=" + testRedirect2File)

      h.textPlainGetWithQueryString("http:" + serverUrl + testEchoFile + "?A=1&B=2")

      h.textPlainGetAcquiringCookie("http:" + serverUrl + testCookieFile)

      h.textPlainOptions("http:" + serverUrl + testPhpFile + "?CT=text/plain")

      h.textPlainPost("http:" + serverUrl + testEchoFile)

      h.textPlainPut("http:" + serverUrl + testEchoFile)

      h.textHtmlDelete("http:" + serverUrl + testEchoFile)

      h.textHtmlOptions("http:" + serverUrl + testEchoFile)

      h.imageJpegGet("http:" + serverUrl + testPhotoFile)

      h.textHtmlGet100("http:" + serverUrl + testPhpFile)

      h.textPlainGetBasicAuth401("http:" + serverUrl + "private/lorem2.txt")
      h.textPlainGetBasicAuth401("https:" + serverUrl + "private/lorem2.txt")
      h.textPlainGetBasicAuth("http:" + serverUrl + "private/lorem2.txt")

      h.textPlainGetAutomaticBasicAuth(hb, "http:" + serverUrl + "private/lorem2.txt")
      total += dt.duration
      println(id + ": " + i + " took " + dt)
    }
    println(id + ": Total time " + total + ", average time per loop " + (total / n))
    total
  }
}

@RunWith(classOf[JUnitRunner])
class HttpIntegration extends FunSuite {

  import uk.co.bigbeeconsultants.http.HttpIntegration._

  def headTest(http: Http, url: String, size: Long) {
    try {
      val response = http.head(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_HTML.mediaType, url)
      expectHeaderIfPresent("gzip")(response.headers, CONTENT_ENCODING)
      //      expectHeaderIfPresent (size === response.headers, CONTENT_LENGTH)
      assert(body.asString === "", url)
    } catch {
      case e: Exception =>
        skipTestWarning("HEAD", url, e)
    }
  }

  private def htmlGet(http: Http, url: String, encoding: String, size: Long) {
    val headers = Headers(ACCEPT_ENCODING -> encoding)
    try {
      val response = http.get(new URL(url), headers)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_HTML.mediaType, url)
      val string = body.asString
      assert(string startsWith "<!DOCTYPE html>", url)
      assert(response.headers(CONTENT_ENCODING).value === encoding, response.headers(CONTENT_ENCODING))
      //assert (size === response.headers.get (CONTENT_LENGTH).toInt)
      val bodyLines = string.split("\n")
      assert(bodyLines(0) startsWith "<!DOCTYPE html>", url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  private def expectHeaderIfPresent(expected: Any)(headers: Headers, name: HeaderName) {
    val hdrs = headers filter name
    if (!hdrs.isEmpty) {
      assert(hdrs(0).value === expected, hdrs(0))
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
    if (e.isInstanceOf[ConnectException] || e.getCause.isInstanceOf[ConnectException] ||
      e.isInstanceOf[UnknownHostException] || e.getCause.isInstanceOf[UnknownHostException]) {
      System.err.println("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
    }
    else {
      throw e
    }
  }

  test("html text/html head x100") {
    val http = new HttpClient(configNoRedirects)
    for (i <- 1 to 100) {
      headTest(http, "http:" + serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
    }
  }

  test("html text/html get x100") {
    val http = new HttpClient(configNoRedirects)
    for (i <- 1 to 100) {
      htmlGet(http, "http:" + serverUrl + testHtmlFile + "?LOREM=" + i, GZIP, testHtmlSize)
    }
  }

  test("html text/html get giving 204 x100") {
    textHtmlGet204("http:" + serverUrl + test204File)
  }

  private def textHtmlGet204(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 204, url)
      val body = response.body
      assert(response.status.isBodyAllowed === false, response.status)
      assert(TEXT_HTML.mediaType === body.contentType.mediaType, url)
      assert(body.asString === "")
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("image/png get x1") {
    imagePngGet("http:" + serverUrl + testImageFile)
  }

  private def imagePngGet(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      assert(response.body.contentType === IMAGE_PNG, url)
      val bytes = response.body.asBytes
      assert(bytes.length === testImageSize, url)
      assert(bytes(1) === 'P')
      assert(bytes(2) === 'N')
      assert(bytes(3) === 'G')
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get x1") {
    textPlainGet("http:" + serverUrl + testTxtFile)
  }

  private def textPlainGet(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      assert(body.toString().startsWith("Lorem "), url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get following redirect") {
    textPlainGetFollowingRedirect("http:" + serverUrl + testRedirect1File + "?TO=" + testRedirect2File)
  }

  private def textPlainGetFollowingRedirect(url: String) {
    val cookie = Cookie("c1", "v1", Domain.localhost)
    val http2 = new HttpClient(configNoRedirects.copy(followRedirects = true))
    try {
      val response = http2.get(new URL(url), gzipHeaders, CookieJar(cookie))
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      assert(body.toString().startsWith("Lorem "), url)
      assert(response.cookies.get.find(_.name == "c1").get === cookie, url)
      assert(response.cookies.get.find(_.name == "redirect1").get.value === "ok", url)
      assert(response.cookies.get.find(_.name == "redirect2").get.value === "ok", url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get following redirect using HttpBrowser") {
    val url = "http:" + serverUrl + testRedirect1File + "?TO=" + testRedirect2File
    val cookie = Cookie("c1", "v1", Domain.localhost)
    val httpBrowser = new HttpBrowser(configNoRedirects.copy(followRedirects = true), initialCookieJar = CookieJar(cookie))
    try {
      val response = httpBrowser.get(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      assert(body.toString().startsWith("Lorem "), url)
      assert(response.cookies.get.find(_.name == "c1").get === cookie, url)
      assert(response.cookies.get.find(_.name == "redirect1").get.value === "ok", url)
      assert(response.cookies.get.find(_.name == "redirect2").get.value === "ok", url)
      assert(httpBrowser.cookies.size === 3, url)
      assert(httpBrowser.cookies.find(_.name == "redirect1").get.value === "ok", url)
      assert(httpBrowser.cookies.find(_.name == "redirect2").get.value === "ok", url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get qith query string x1") {
    textPlainGetWithQueryString("http:" + serverUrl + testEchoFile + "?A=1&B=2")
  }

  private def textPlainGetWithQueryString(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      val bodyLines = response.body.toString().split("\n").toSeq
      assert(extractLineFromResponse("CONTENT_LENGTH", bodyLines) === "", response.body)
      assert(extractLineFromResponse("CONTENT_TYPE", bodyLines) === "", response.body)
      assert(bodyLines.filter(_.startsWith("GET:")).map(_.substring(5)).toSet === Set("A: 1", "B: 2"), response.body)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get acquiring a cookie") {
    textPlainGetAcquiringCookie("http:" + serverUrl + testCookieFile)
  }

  private def textPlainGetAcquiringCookie(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders, CookieJar.Empty)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      val cookies = response.cookies.get
      assert(cookies.size === 1, url)
      assert(cookies.get("c1").get.value === "v1", url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/plain options x1") {
    textPlainOptions("http:" + serverUrl + testPhpFile + "?CT=text/plain")
  }

  private def textPlainOptions(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.options(new URL(url), None)
      assert(response.status.code === 302, url)
      val body = response.body
      assert(body.asString.length === 0, url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/plain post x1") {
    textPlainPost("http:" + serverUrl + testEchoFile)
  }

  private def textPlainPost(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.post(new URL(url), Some(jsonBody), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      val bodyLines = response.body.asString.split("\n").toSeq
      assert(extractLineFromResponse("CONTENT_LENGTH", bodyLines) === jsonSample.length.toString)
      assert(extractLineFromResponse("CONTENT_TYPE", bodyLines) === APPLICATION_JSON.mediaType)
      assert(extractLineFromResponse("PUT", bodyLines) === jsonSample)
    } catch {
      case e: Exception =>
        skipTestWarning("POST", url, e)
    }
  }

  test("php text/plain put x1") {
    textPlainPut("http:" + serverUrl + testEchoFile)
  }

  private def textPlainPut(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.put(new URL(url), jsonBody, gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      val bodyLines = response.body.asString.split("\n").toSeq
      assert(extractLineFromResponse("CONTENT_LENGTH", bodyLines) === jsonSample.length.toString)
      assert(extractLineFromResponse("CONTENT_TYPE", bodyLines) === APPLICATION_JSON.mediaType)
      assert(extractLineFromResponse("PUT", bodyLines) === jsonSample)
    } catch {
      case e: Exception =>
        skipTestWarning("PUT", url, e)
    }
  }

  test("php text/html delete x1") {
    textHtmlDelete("http:" + serverUrl + testEchoFile)
  }

  private def textHtmlDelete(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.delete(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      val bodyLines = response.body.asString.split("\n").toSeq
      assert(extractLineFromResponse("REQUEST_METHOD", bodyLines) === "DELETE")
    } catch {
      case e: Exception =>
        skipTestWarning("DELETE", url, e)
    }
  }

  test("php text/html options x1") {
    textHtmlOptions("http:" + serverUrl + testEchoFile)
  }

  private def textHtmlOptions(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.options(new URL(url), None)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      val bodyLines = response.body.asString.split("\n").toSeq
      assert(extractLineFromResponse("REQUEST_METHOD", bodyLines) === "OPTIONS")
    } catch {
      case e: Exception =>
        skipTestWarning("DELETE", url, e)
    }
  }

  test("jpg image/jpg get x100") {
    imageJpegGet("http:" + serverUrl + testPhotoFile)
  }

  private def imageJpegGet(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val loops = 1
      val timer = new DiagnosticTimer
      for (i <- 1 to loops) {
        val response = http.get(new URL(url + "?n=" + i), gzipHeaders)
        assert(response.status.code === 200, url)
        assert(response.body.contentType.mediaType === IMAGE_JPG.mediaType, url)
        val bytes = response.body.asBytes
        assert(bytes.length === testPhotoSize, url)
      }
      val duration = timer.duration.microseconds
      val bytes = BigDecimal(testPhotoSize * loops)
      val rate = bytes / duration
      println(bytes + " bytes took " + timer.duration + " at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/html get x100") {
    textHtmlGet100("http:" + serverUrl + testPhpFile)
  }

  private def textHtmlGet100(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      var size = -1
      var first = "NOT SET"
      val loops = 20
      val timer = new DiagnosticTimer
      var ok = true
      for (i <- 1 to loops) {
        if (ok) {
          try {
            val is = i.toString
            val response = http.get(new URL(url + "?STUM=1"), gzipHeaders)
            assert(response.status.code === 200, url)
            val body = response.body
            assert(body.contentType.mediaType === TEXT_HTML.mediaType, url)
            val string = body.asString
            assert(string.startsWith("<html>"), url)
            if (size < 0) {
              first = string
              size = first.length
            } else {
              assert(string === first, is)
              assert(string.length === size, is)
            }
          } catch {
            case e: Exception =>
              skipTestWarning("GET", "soakTestTextOK " + i, e)
              ok = false
          }
        }
      }

      val duration = timer.duration.microseconds
      val bytes = BigDecimal(size * loops)
      val rate = if (duration > 0) bytes / duration else 0
      println(bytes + " bytes took " + timer.duration + " at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get basic auth") {
    textPlainGetBasicAuth401("http:" + serverUrl + "private/lorem2.txt")
    textPlainGetBasicAuth("http:" + serverUrl + "private/lorem2.txt")
  }

  private def textPlainGetBasicAuth401(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 401, url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  private def textPlainGetBasicAuth(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val bigbee = new Credential("bigbee", "HelloWorld")
      val requestHeaders = gzipHeaders + bigbee.toBasicAuthHeader
      val response = http.get(new URL(url), requestHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      assert(body.asString.startsWith("Lorem "), url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get automatic basic auth") {
    val bigbee = new Credential("bigbee", "HelloWorld")
    val http = new HttpBrowser(configNoRedirects, CookieJar.Empty, new CredentialSuite(Map("Restricted" -> bigbee)))
    for (i <- 1 to 5) {
      textPlainGetAutomaticBasicAuth(http, "http:" + serverUrl + "private/lorem2.txt")
    }
  }

  private def textPlainGetAutomaticBasicAuth(http: Http, url: String) {
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === TEXT_PLAIN.mediaType, url)
      assert(body.asString.startsWith("Lorem "), url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

}
