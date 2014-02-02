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
  val cache = new InMemoryCache()
  val hb1 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds)
  val hb2 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds, cache)

  /** Provides a single-threaded soak-tester. */
  def main(args: Array[String]) {

    val concurrency = 2
    val nLoops = 5

    val futures1 = for (i <- 1 to concurrency) yield {
      Futures.future {
        instance("t" + i, nLoops, hb1)
      }
    }

    val futures2 = for (i <- 1 to concurrency) yield {
      Futures.future {
        instance("t" + i, nLoops, hb2)
      }
    }

    val totals1 = Futures.awaitAll(10000000, futures1: _*).map(_.get).map(_.asInstanceOf[Duration])
    val totals2 = Futures.awaitAll(10000000, futures2: _*).map(_.get).map(_.asInstanceOf[Duration])
    val t1 = totals1.foldLeft(Duration.Zero)(_ + _)
    val t2 = totals2.foldLeft(Duration.Zero)(_ + _)
    //    val t2 = Duration.Zero
    println("Grand totals: without " + t1 + ", with " + t2)
    //Thread.sleep(60000) // time for inspecting any lingering network connections
  }

  def instance(id: String, n: Int, hb: HttpBrowser) = {
    val h = new HttpCaching

    var total = Duration(0L)
    for (i <- 1 to n) {
      println(id + ": iteration " + i)
      val dt = new DiagnosticTimer
      h.htmlGet(hb, "http://vm05.spikeislandband.org.uk/?LOREM=" + i, MediaType.TEXT_HTML, GZIP)
      h.htmlGet(hb, "http://vm05.spikeislandband.org.uk/css/_index.css", MediaType.TEXT_CSS, GZIP)
      total += dt.duration
      println(id + ": " + i + " took " + dt)
    }
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
