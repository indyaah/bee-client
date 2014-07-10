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
import uk.co.bigbeeconsultants.http.response.{Status, Response}
import uk.co.bigbeeconsultants.http.request.Request
import java.util.concurrent.{ExecutorService, Executors}
import uk.co.bigbeeconsultants.http.cache.CacheConfig

object HttpCaching {

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Some(Proxy.NO_PROXY)

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)
  val configNoRedirects = Config(connectTimeout = 20000, followRedirects = false, proxy = proxy).allowInsecureSSL

  val bigbee = new Credential("bigbee", "HelloWorld")
  val creds = new CredentialSuite(Map("Restricted" -> bigbee))
  val hb1 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds, CacheConfig(enabled = false))
  val hb2 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds, CacheConfig(enabled = true, lazyCleanup = false))
  val hb3 = new HttpBrowser(configNoRedirects, CookieJar.Empty, creds, CacheConfig(enabled = true, lazyCleanup = true))

  val concurrency = 1
  val nLoops = 100

  //    val htmUrl = "http://beeclient/test-lighthttpclient.html"
//  val cssUrl = "http://beeclient/small.css"
  val cssUrl = "URL:http://vm05.spikeislandband.org.uk/css/_index.css"
  val txtUrl = "http://beeclient/empty.txt" // zero size
//  val jpgUrl = "http://beeclient/plataria-sunset.jpg" // about 1.6MB
  val jpgUrl = "http://vm05.spikeislandband.org.uk/logo/sib-logo-230.png" // about 32KB

  /** Provides a single-threaded soak-tester. */
  def main(args: Array[String]) {
    val pool = Executors.newFixedThreadPool(concurrency)

//    trial(pool, "xcache txt1 th", txtUrl, MediaType.TEXT_PLAIN, nLoops, hb1)
//    trial(pool, "cache2 txt1 th", txtUrl, MediaType.TEXT_PLAIN, nLoops, hb2)
//    trial(pool, "cache3 txt1 th", txtUrl, MediaType.TEXT_PLAIN, nLoops, hb3)

    trial(pool, "xcache jpg1 th", jpgUrl, MediaType.IMAGE_JPG, nLoops, hb1)
//    trial(pool, "cache2 jpg1 th", jpgUrl, MediaType.IMAGE_JPG, nLoops, hb2)
    trial(pool, "cache3 jpg1 th", jpgUrl, MediaType.IMAGE_JPG, nLoops, hb3)

//    trial(pool, "xcache css1 th", cssUrl, MediaType.TEXT_CSS, nLoops, hb1)
//    trial(pool, "cache2 css1 th", cssUrl, MediaType.TEXT_CSS, nLoops, hb2)
//    trial(pool, "cache3 css1 th", cssUrl, MediaType.TEXT_CSS, nLoops, hb3)

//    trial(pool, "xcache css1 th", cssUrl, MediaType.TEXT_CSS, nLoops, hb1)
//    trial(pool, "cache2 css1 th", cssUrl, MediaType.TEXT_CSS, nLoops, hb2)
//    trial(pool, "cache3 css1 th", cssUrl, MediaType.TEXT_CSS, nLoops, hb3)

    pool.shutdown()
    //Thread.sleep(60000) // time for inspecting any lingering network connections
  }

  def trial(pool: ExecutorService, id: String, path: String, mediaType: MediaType, nLoops: Int, hb: HttpBrowser) {
    // JVM warm-up
    for (i <- 1 to 2) {
      htmlGet(hb, path, mediaType, GZIP)
    }
    for (i <- 1 to concurrency) {
      pool.submit(new Runnable() {
        def run() {
          instance(id + i, path, mediaType, nLoops, hb)
        }
      })
    }
  }

  def instance(id: String, path: String, mediaType: MediaType, n: Int, hb: HttpBrowser) = {
    // cache filler
    htmlGet(hb, path, mediaType, GZIP)

    val dt = new DiagnosticTimer
    for (i <- 1 to n) {
      htmlGet(hb, path, mediaType, GZIP)
    }

    val total = dt.duration
    println(id + ": Total time " + total + ", average time per loop " + (total / n))
    total
  }

  private def htmlGet(http: Http, url: String, mediaType: MediaType, encoding: String): Response = {
    val headers = Headers(ACCEPT_ENCODING -> encoding)
    try {
      val response = http.get(new URL(url), headers)
//      assert(response.status.code === 200, url)
      val body = response.body
//      assert(body.contentType.mediaType === mediaType.mediaType, url)
      //      val string = body.asString
      val contentEncodiing = response.headers.get(CONTENT_ENCODING)
//      if (contentEncodiing.isDefined)
//        assert(contentEncodiing.get.value === encoding, response.headers(CONTENT_ENCODING))
      response
    } catch {
      case e: Exception =>
        skipTestWarning("GET", url, e)
        Response(Request.get(url), Status.S200_OK, MediaType.TEXT_PLAIN, "", Headers())
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


class HttpCaching extends FunSuite {

  import HttpCaching._

  test("html image/jpg get x10") {
    val jpgUrl = "http://beeclient/plataria-sunset.jpg" // about 1.6MB
    val pool = Executors.newFixedThreadPool(3)
    val n = 2
    trial(pool, "xcache jpg1 th", jpgUrl, MediaType.IMAGE_JPG, n, hb1)
    trial(pool, "cache2 jpg1 th", jpgUrl, MediaType.IMAGE_JPG, n, hb2)
    trial(pool, "cache3 jpg1 th", jpgUrl, MediaType.IMAGE_JPG, n, hb3)
    Thread.sleep(750)
    pool.shutdown()
  }

  test("html text/css get x100") {
    val cssUrl = "http://beeclient/small.css"
    val pool = Executors.newFixedThreadPool(5)
    val n = 100
    trial(pool, "xcache css1 th", cssUrl, MediaType.TEXT_CSS, n, hb1)
    trial(pool, "cache2 css1 th", cssUrl, MediaType.TEXT_CSS, n, hb2)
    trial(pool, "cache3 css1 th", cssUrl, MediaType.TEXT_CSS, n, hb3)
    Thread.sleep(750)
    pool.shutdown()
  }
}
