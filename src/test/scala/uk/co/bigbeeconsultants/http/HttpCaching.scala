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

import java.net.{Proxy, UnknownHostException, ConnectException, URL}
import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http.HttpClient._
import uk.co.bigbeeconsultants.http.header.{MediaType, CookieJar, Headers}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.util.DiagnosticTimer
import uk.co.bigbeeconsultants.http.auth.{CredentialSuite, Credential}
import uk.co.bigbeeconsultants.http.cache.InMemoryCache
import uk.co.bigbeeconsultants.http.response.{Status, Response}
import uk.co.bigbeeconsultants.http.request.Request
import java.util.concurrent.{ExecutorService, Executors}

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

  val concurrency = 5
  val nLoops = 5000

  /** Provides a single-threaded soak-tester. */
  def main(args: Array[String]) {
    val pool = Executors.newFixedThreadPool(concurrency)

    //    val htmUrl = "http://beeclient/test-lighthttpclient.html"
    //    val cssUrl = "http://beeclient/index.css"
    val txtUrl = "http://beeclient/empty.txt" // zero size
    val jpgUrl = "http://beeclient/plataria-sunset.jpg" // about 1.6MB

    trial(pool, "xcache txt1 th", txtUrl, MediaType.TEXT_PLAIN, hb1)
    trial(pool, "cache2 txt1 th", txtUrl, MediaType.TEXT_PLAIN, hb2)
    trial(pool, "cache3 txt1 th", txtUrl, MediaType.TEXT_PLAIN, hb3)

    //    trial(pool, "xcache jpg1 th", jpgUrl, MediaType.IMAGE_JPG, hb1)
    //    trial(pool, "cache2 jpg1 th", jpgUrl, MediaType.IMAGE_JPG, hb2)
    //    trial(pool, "cache3 jpg1 th", jpgUrl, MediaType.IMAGE_JPG, hb3)

    pool.shutdown()
    //Thread.sleep(60000) // time for inspecting any lingering network connections
  }

  def trial(pool: ExecutorService, id: String, path: String, mediaType: MediaType, hb: HttpBrowser) {
    for (i <- 1 to concurrency) {
      pool.submit(new Runnable() {
        def run() {
          instance(id + i, path, mediaType, nLoops, hb)
        }
      })
    }
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
      val contentEncodiing = response.headers.get(CONTENT_ENCODING)
      if (contentEncodiing.isDefined)
        assert(contentEncodiing.get.value === encoding, response.headers(CONTENT_ENCODING))
      response
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
        Response(Request.get(url), Status.S200_OK, MediaType.TEXT_PLAIN, "", Headers())
    }
  }

  test("html text/html get x10") {
    for (i <- 1 to 10) {
      hb2.cache.clear()
      val nc = htmlGet(hb2, "http://beeclient/test-lighthttpclient.html?LOREM=" + i, MediaType.TEXT_HTML, GZIP)
      for (j <- 1 to 5) {
        val c = htmlGet(hb2, "http://beeclient/test-lighthttpclient.html?LOREM=" + i, MediaType.TEXT_HTML, GZIP)
        assert(c.body.contentLength === nc.body.contentLength)
      }
    }
  }

  test("html text/css get x100") {
    for (i <- 1 to 1) {
      hb2.cache.clear()
      val nc = htmlGet(hb1, "http://beeclient/index.css", MediaType.TEXT_CSS, GZIP)
      for (j <- 1 to 5) {
        val c = htmlGet(hb2, "http://beeclient/index.css", MediaType.TEXT_CSS, GZIP)
        assert(c.body.contentLength === nc.body.contentLength)
      }
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
