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

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http.HttpClient._
import uk.co.bigbeeconsultants.http.header.{MediaType, CookieJar, Headers}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import java.net.{Proxy, UnknownHostException, ConnectException, URL}
import scala.actors.Futures
import uk.co.bigbeeconsultants.http.util.{DiagnosticTimer, Duration}
import uk.co.bigbeeconsultants.http.auth.{CredentialSuite, Credential}
import uk.co.bigbeeconsultants.http.cache.InMemoryCache
import uk.co.bigbeeconsultants.http.response.{Status, Response, StringResponseBody}
import uk.co.bigbeeconsultants.http.request.Request

object HttpCaching {

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Some(Proxy.NO_PROXY)

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)
  val configNoRedirects = Config(connectTimeout = 20000, followRedirects = false, proxy = proxy).allowInsecureSSL

  val bigbee = new Credential("bigbee", "HelloWorld")
  val creds = new CredentialSuite(Map("Restricted" -> bigbee))
  val cache1 = new InMemoryCache(lazyCleanup = false)
  val cache2 = new InMemoryCache(lazyCleanup = true)
  val hb1 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds)
  val hb2 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds, cache1)
  val hb3 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds, cache2)

  val concurrency = 1
  val nLoops = 1

  /** Provides a single-threaded soak-tester. */
  def main(args: Array[String]) {

//    val htmUrl = "http://beeclient/test-lighthttpclient.html"
//    val cssUrl = "http://beeclient/index.css"

    val htmUrl = "http://vm05.spikeislandband.org.uk/"
    val cssUrl = "http://vm05.spikeislandband.org.uk/css/_index.css"

//    val nchtm = trial("no cache htm th", htmUrl, MediaType.TEXT_HTML, hb1)
    val nccss = trial("no cache css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val ca1htm = trial("cache1 htm th", htmUrl, MediaType.TEXT_HTML, hb2)
    val ca1css = trial("cache1 css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val ca2htm = trial("cache2 htm th", htmUrl, MediaType.TEXT_HTML, hb2)
    val ca2css = trial("cache2 css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val ca3htm = trial("cache3 htm th", htmUrl, MediaType.TEXT_HTML, hb2)
    val ca3css = trial("cache3 css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val ca4htm = trial("cache4 htm th", htmUrl, MediaType.TEXT_HTML, hb2)
    val ca4css = trial("cache4 css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val ca5htm = trial("cache5 htm th", htmUrl, MediaType.TEXT_HTML, hb2)
    val ca5css = trial("cache5 css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val ca6htm = trial("cache6 htm th", htmUrl, MediaType.TEXT_HTML, hb2)
    val ca6css = trial("cache6 css th", cssUrl, MediaType.TEXT_CSS, hb2)

//    val wo = nchtm + nccss
//    val wi1 = ca1htm + ca1css
//    val wi2 = ca2htm + ca2css
//    println("Grand totals: without " + nchtm + "+" + nccss + "=" + wo +
//      ", with1 " + ca1htm + "+" + ca1css + "=" + wi1 +
//      ", with2 " + ca2htm + "+" + ca2css + "=" + wi2)
    //Thread.sleep(60000) // time for inspecting any lingering network connections
  }

  def trial(id: String, path: String, mediaType: MediaType, hb: HttpBrowser) = {
    val futures1 = for (i <- 1 to concurrency) yield {
      Futures.future {
        instance(id + i, path, mediaType, nLoops, hb)
      }
    }

    val veryLongTime = 10000000
    val totals1 = Futures.awaitAll(veryLongTime, futures1: _*).map(_.get).map(_.asInstanceOf[Duration])
    totals1.foldLeft(Duration.Zero)(_ + _)
  }

  def instance(id: String, path: String, mediaType: MediaType, n: Int, hb: HttpBrowser) = {
    val h = new HttpCaching

    val dt = new DiagnosticTimer
    for (i <- 1 to n) {
      h.htmlGet(hb, path, mediaType, GZIP)
    }
    val total = dt.duration
    println(id + ": Total time " + total + ", average time per loop " + (total / n))
    total
  }
}

class HttpCaching extends FunSuite {

  import HttpCaching._

  private def htmlGet(http: Http, url: String, mediaType: MediaType, encoding: String): Response = {
    val headers = Headers(ACCEPT_ENCODING -> encoding)
    try {
      val response = http.get(new URL(url), headers)
      assert(response.status.code === 200, url)
      val body = response.body
      assert(body.contentType.mediaType === mediaType.mediaType, url)
      //      val string = body.asString
      assert(response.headers(CONTENT_ENCODING).value === encoding, response.headers(CONTENT_ENCODING))
      response
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
        Response(Request.get(url), Status.S200_OK, MediaType.TEXT_PLAIN, "", Headers())
    }
  }

  test("html text/html get x100") {
    for (i <- 1 to 1) {
      hb2.cache.clear()
      val nc = htmlGet(hb2, "http://beeclient/test-lighthttpclient.html?LOREM=" + i, MediaType.TEXT_HTML, GZIP)
      for (i <- 1 to 1) {
        val c = htmlGet(hb2, "http://beeclient/test-lighthttpclient.html?LOREM=" + i, MediaType.TEXT_HTML, GZIP)
        assert(c.body.contentLength === nc.body.contentLength)
      }
    }
  }

  ignore("html text/css get x100") {
    for (i <- 1 to 1) {
      htmlGet(hb1, "http://beeclient/index.css?LOREM=" + i, MediaType.TEXT_CSS, GZIP)
      //      htmlGet(http, "https:" + serverUrl + testHtmlFile + "?LOREM=" + i, GZIP, testHtmlSize)
      //htmlGet(http, "http:" + serverUrl + testHtmlFile + "?LOREM=" + i, DEFLATE, testHtmlSize)
    }
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

}
