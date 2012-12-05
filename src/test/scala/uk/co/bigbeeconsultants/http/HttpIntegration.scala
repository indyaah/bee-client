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

import auth.{Realm, CredentialSuite, Credential}
import HttpClient._
import header._
import header.MediaType._
import header.HeaderName._
import java.lang.AssertionError
import request.RequestBody
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.{UnknownHostException, ConnectException, Proxy, URL}
import java.io.File
import scala.Some
import util.DumbTrustManager
import header.{HeaderName, Headers}

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

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)

  val dir = new File("src/test/resources")
  val testHtmlSize = 1497
  val testTxtSize = new File(dir, testTxtFile).length
  val testImageSize = 497
  val testPhotoSize = 1605218
}

class HttpIntegration extends FunSuite with BeforeAndAfter {

  import HttpIntegration._

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Proxy.NO_PROXY

  DumbTrustManager.install()

  private val jsonSample = """{ "x": 1, "y": true }"""
  private val jsonBody = RequestBody(jsonSample, APPLICATION_JSON)

  val configNoRedirects = Config(followRedirects = false, proxy = proxy)


  def headTest(http: Http, url: String, size: Long) {
    try {
      val response = http.head(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.value === TEXT_HTML.value, url)
      expectHeaderIfPresent("gzip")(response.headers, CONTENT_ENCODING)
      //      expectHeaderIfPresent (size === response.headers, CONTENT_LENGTH)
      assert(body.toString === "", url)
    } catch {
      case e: Exception =>
        skipTestWarning("HEAD", url, e)
    }
  }

  private def htmlGet(http: Http, url: String, size: Long) {
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.value === TEXT_HTML.value, url)
      val string = body.toString
      assert(string startsWith "<!DOCTYPE html>", url)
      assert(response.headers(CONTENT_ENCODING).value === "gzip", response.headers(CONTENT_ENCODING))
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
      headTest(http, "https:" + serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
    }
  }

  test("html text/html get x100") {
    val http = new HttpClient(configNoRedirects)
    for (i <- 1 to 100) {
      htmlGet(http, "http:" + serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
      htmlGet(http, "https:" + serverUrl + testHtmlFile + "?LOREM=" + i, testHtmlSize)
    }
  }

  test("html text/html get giving 204 x100") {
    textHtmlGet204("http:" + serverUrl + test204File)
    //    textHtmlGet204("https:" + serverUrl + test204File)
  }

  private def textHtmlGet204(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(204 === response.status.code, url)
      val body = response.body
      assert(false === response.status.isBodyAllowed, response.status)
      assert(TEXT_HTML.value === body.contentType.value, url)
      assert("" === body.toString)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("image/png get x1") {
    imagePngGet("http:" + serverUrl + testImageFile)
    imagePngGet("https:" + serverUrl + testImageFile)
  }

  private def imagePngGet(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(200 === response.status.code, url)
      assert(IMAGE_PNG === response.body.contentType, url)
      val bytes = response.body.asBytes
      assert(testImageSize === bytes.length, url)
      assert('P' === bytes(1))
      assert('N' === bytes(2))
      assert('G' === bytes(3))
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get x1") {
    textPlainGet("http:" + serverUrl + testTxtFile)
    textPlainGet("https:" + serverUrl + testTxtFile)
  }

  private def textPlainGet(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      assert(true === body.toString.startsWith("Lorem "), url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get following redirect") {
    textPlainGetFollowingRedirect("http:" + serverUrl + testRedirect1File + "?TO=" + testRedirect2File)
    //    textPlainGetFollowingRedirect("https:" + serverUrl + testRedirect1File + "?TO=" + testRedirect2File)
  }

  private def textPlainGetFollowingRedirect(url: String) {
    val cookie = Cookie("c1", "v1", Domain.localhost)
    val http2 = new HttpClient(Config(followRedirects = true))
    try {
      val response = http2.get(new URL(url), gzipHeaders, CookieJar(cookie))
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      assert(true === body.toString.startsWith("Lorem "), url)
      assert(cookie === response.cookies.get.find(_.name == "c1").get, url)
      assert("ok" === response.cookies.get.find(_.name == "redirect1").get.value, url)
      assert("ok" === response.cookies.get.find(_.name == "redirect2").get.value, url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get following redirect using HttpBrowser") {
    val url = "http:" + serverUrl + testRedirect1File + "?TO=" + testRedirect2File
    val cookie = Cookie("c1", "v1", Domain.localhost)
    val httpBrowser = new HttpBrowser(Config(followRedirects = true), initialCookieJar = CookieJar(cookie))
    try {
      val response = httpBrowser.get(new URL(url), gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      assert(true === body.toString.startsWith("Lorem "), url)
      assert(cookie === response.cookies.get.find(_.name == "c1").get, url)
      assert("ok" === response.cookies.get.find(_.name == "redirect1").get.value, url)
      assert("ok" === response.cookies.get.find(_.name == "redirect2").get.value, url)
      assert(3 === httpBrowser.cookies.size, url)
      assert("ok" === httpBrowser.cookies.find(_.name == "redirect1").get.value, url)
      assert("ok" === httpBrowser.cookies.find(_.name == "redirect2").get.value, url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get qith query string x1") {
    textPlainGetWithQueryString("http:" + serverUrl + testEchoFile + "?A=1&B=2")
    //    textPlainGetWithQueryString("https:" + serverUrl + testEchoFile + "?A=1&B=2")
  }

  private def textPlainGetWithQueryString(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      val bodyLines = response.body.toString.split("\n").toSeq
      assert("" === extractLineFromResponse("CONTENT_LENGTH", bodyLines), response.body)
      assert("" === extractLineFromResponse("CONTENT_TYPE", bodyLines), response.body)
      assert(Set("A: 1", "B: 2") === bodyLines.filter(_.startsWith("GET:")).map(_.substring(5)).toSet, response.body)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get acquiring a cookie") {
    textPlainGetAcquiringCookie("http:" + serverUrl + testCookieFile)
    //    textPlainGetAcquiringCookie("https:" + serverUrl + testCookieFile)
  }

  private def textPlainGetAcquiringCookie(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders, CookieJar.empty)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      val cookies = response.cookies.get
      assert(1 === cookies.size, url)
      assert("v1" === cookies.get("c1").get.value, url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/plain options x1") {
    textPlainOptions("http:" + serverUrl + testPhpFile + "?CT=text/plain")
    //    textPlainOptions("https:" + serverUrl + testPhpFile + "?CT=text/plain")
  }

  private def textPlainOptions(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.options(new URL(url), None)
      assert(302 === response.status.code, url)
      val body = response.body
      assert(0 === body.toString.length, url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("php text/plain post x1") {
    textPlainPost("http:" + serverUrl + testEchoFile)
    //    textPlainPost("https:" + serverUrl + testEchoFile)
  }

  private def textPlainPost(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.post(new URL(url), Some(jsonBody), gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      val bodyLines = response.body.toString.split("\n").toSeq
      assert(jsonSample.length.toString === extractLineFromResponse("CONTENT_LENGTH", bodyLines))
      assert(APPLICATION_JSON.value === extractLineFromResponse("CONTENT_TYPE", bodyLines))
      assert(jsonSample === extractLineFromResponse("PUT", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("POST", url, e)
    }
  }

  test("php text/plain put x1") {
    textPlainPut("http:" + serverUrl + testEchoFile)
    //    textPlainPut("https:" + serverUrl + testEchoFile)
  }

  private def textPlainPut(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.put(new URL(url), jsonBody, gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      val bodyLines = response.body.toString.split("\n").toSeq
      assert(jsonSample.length.toString === extractLineFromResponse("CONTENT_LENGTH", bodyLines))
      assert(APPLICATION_JSON.value === extractLineFromResponse("CONTENT_TYPE", bodyLines))
      assert(jsonSample === extractLineFromResponse("PUT", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("PUT", url, e)
    }
  }

  test("php text/html delete x1") {
    textHtmlDelete("http:" + serverUrl + testEchoFile)
    //    textHtmlDelete("https:" + serverUrl + testEchoFile)
  }

  private def textHtmlDelete(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.delete(new URL(url), gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      val bodyLines = response.body.toString.split("\n").toSeq
      assert("DELETE" === extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("DELETE", url, e)
    }
  }

  test("php text/html options x1") {
    textHtmlOptions("http:" + serverUrl + testEchoFile)
    //    textHtmlOptions("https:" + serverUrl + testEchoFile)
  }

  private def textHtmlOptions(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.options(new URL(url), None)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      val bodyLines = response.body.toString.split("\n").toSeq
      assert("OPTIONS" === extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning("DELETE", url, e)
    }
  }

  test("jpg image/jpg get x100") {
    imageJpegGet("http:" + serverUrl + testPhotoFile)
    // works but VERY slow
    //imageJpegGet("https:" + serverUrl + testPhotoFile)
  }

  private def imageJpegGet(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val loops = 20
      val before = System.currentTimeMillis()
      for (i <- 1 to loops) {
        val response = http.get(new URL(url + "?n=" + i), gzipHeaders)
        assert(200 === response.status.code, url)
        assert(IMAGE_JPG.value === response.body.contentType.value, url)
        val bytes = response.body.asBytes
        assert(testPhotoSize === bytes.length, url)
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
    textHtmlGet100("http:" + serverUrl + testPhpFile)
    //    textHtmlGet100("https:" + serverUrl + testPhpFile)
  }

  private def textHtmlGet100(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      var size = -1
      var first = "NOT SET"
      val loops = 20
      val before = System.currentTimeMillis()
      var ok = true
      for (i <- 1 to loops) {
        if (ok) {
          try {
            val is = i.toString
            val response = http.get(new URL(url + "?STUM=1"), gzipHeaders)
            assert(200 === response.status.code, url)
            val body = response.body
            assert(TEXT_HTML.value === body.contentType.value, url)
            val string = body.toString
            assert(string.startsWith("<html>"), url)
            if (size < 0) {
              first = string
              size = first.length
            } else {
              assert(first === string, is)
              assert(size === string.length, is)
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

  test("txt text/plain get basic auth") {
    textPlainGetBasicAuth401("http:" + serverUrl + "private/lorem2.txt")
    textPlainGetBasicAuth401("https:" + serverUrl + "private/lorem2.txt")
    textPlainGetBasicAuth("http:" + serverUrl + "private/lorem2.txt")
    //textPlainGetBasicAuth("https:" + serverUrl + "private/lorem2.txt")
  }

  private def textPlainGetBasicAuth401(url: String) {
    val http = new HttpClient(configNoRedirects)
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(401 === response.status.code, url)
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
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      assert(true === body.toString.startsWith("Lorem "), url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

  test("txt text/plain get automatic basic auth") {
    for (i <- 1 to 5) {
      textPlainGetAutomaticBasicAuth("http:" + serverUrl + "private/lorem2.txt")
    }
  }

  private def textPlainGetAutomaticBasicAuth(url: String) {
    val bigbee = new Credential("bigbee", "HelloWorld")
    val http = new HttpBrowser(configNoRedirects, CookieJar.empty, new CredentialSuite(Map(Realm("Restricted") -> bigbee)))
    try {
      val response = http.get(new URL(url), gzipHeaders)
      assert(200 === response.status.code, url)
      val body = response.body
      assert(TEXT_PLAIN.value === body.contentType.value, url)
      assert(true === body.toString.startsWith("Lorem "), url)
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
    }
  }

}
