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
import header.MediaType._
import header.HeaderName._
import java.lang.AssertionError
import java.net.{Proxy, URL}
import header._
import org.scalatest.Assertions
import request.RequestBody
import util.DumbTrustManager
import scala.Predef._

/**
 * Tests the API against httpbin.org. This is an app rather than a unit test because it would be rude to
 * hammer their site on every automatic rebuild.
 */
object HttpBin extends App with Assertions {

  DumbTrustManager.install()

  private val jsonSample = """{ "x": 1, "y": true }"""
  private val jsonBody = RequestBody(jsonSample, APPLICATION_JSON)

  val serverUrl = "httpbin.org"

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Proxy.NO_PROXY

  implicit val config = Config(followRedirects = false, proxy = proxy)
  var httpClient = new HttpClient(config)
  var httpBrowser = new HttpBrowser(config)

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)

  private def expectHeaderIfPresent(expected: Any)(headers: Headers, name: HeaderName) {
    val hdrs = headers filter name
    if (!hdrs.isEmpty) {
      assert(hdrs(0).value == expected, hdrs(0))
    }
  }

  private def extractLineFromResponse(expectedHeader: String, bodyLines: Seq[String]): String = {
    val expectedHeaderColon = expectedHeader + ':'
    for (line <- bodyLines) {
      if (line.trim.startsWith(expectedHeaderColon)) {
        return line.substring(line.indexOf(':') + 1).trim()
      }
    }
    throw new AssertionError("Expect response to contain\n" + expectedHeader)
  }


  headTest(httpClient, "http://" + serverUrl)
  headTest(httpClient, "https://" + serverUrl)
  headTest(httpBrowser, "http://" + serverUrl)
  headTest(httpBrowser, "https://" + serverUrl)

  def headTest(http: Http, url: String) {
    println("HEAD " + url)
    val response = http.head(new URL(url), gzipHeaders)
    assert(response.status.code === 200, url)
    val body = response.body
    assert(body.contentType.value === TEXT_HTML.value, url)
    expectHeaderIfPresent("gzip")(response.headers, CONTENT_ENCODING)
    assert(body.toString == "", url)
  }


  htmlGet(httpClient, "http://" + serverUrl + "/headers")
  htmlGet(httpClient, "https://" + serverUrl + "/headers")
  htmlGet(httpBrowser, "http://" + serverUrl + "/headers")
  htmlGet(httpBrowser, "https://" + serverUrl + "/headers")

  private def htmlGet(http: Http, url: String) {
    println("GET " + url)
    val response = http.get(new URL(url), gzipHeaders)
    assert(response.status.code === 200, url)
    val body = response.body
    assert(body.contentType.value === APPLICATION_JSON.value, url)
    val string = body.toString
    assert(string startsWith "{", url)
    val bodyLines = string.split("\n")
    assert(bodyLines(0) startsWith "{", url)
  }


  textHtmlGet204(httpClient, "http://" + serverUrl + "/status/204")
  textHtmlGet204(httpClient, "https://" + serverUrl + "/status/204")
  textHtmlGet204(httpBrowser, "http://" + serverUrl + "/status/204")
  textHtmlGet204(httpBrowser, "https://" + serverUrl + "/status/204")

  private def textHtmlGet204(http: Http, url: String) {
    println("GET " + url)
    val response = http.get(new URL(url), gzipHeaders)
    assert(204 === response.status.code, url)
    val body = response.body
    assert(false === response.status.isBodyAllowed, response.status)
    assert(TEXT_HTML.value === body.contentType.value, url)
    assert("" === body.toString)
  }


  val cookieC1V1 = Cookie("c1", "v1", Domain(serverUrl))
  val configFollowRedirects = Config(followRedirects = true)
  textPlainGetFollowingRedirect(new HttpClient(configFollowRedirects), "http://" + serverUrl + "/redirect/1")
  textPlainGetFollowingRedirect(new HttpClient(configFollowRedirects), "https://" + serverUrl + "/redirect/1")
  textPlainGetFollowingRedirect(new HttpBrowser(configFollowRedirects, CookieJar(cookieC1V1)), "http://" + serverUrl + "/redirect/1")
  textPlainGetFollowingRedirect(new HttpBrowser(configFollowRedirects, CookieJar(cookieC1V1)), "https://" + serverUrl + "/redirect/1")

  private def textPlainGetFollowingRedirect(http: Http, url: String) {
    println("GET " + url)
    val response = http.get(new URL(url), gzipHeaders, CookieJar(cookieC1V1))
    assert(200 === response.status.code, url)
    val body = response.body
    assert(APPLICATION_JSON.value === body.contentType.value, url)
    assert(true === body.toString.startsWith("{"), url)
    assert(cookieC1V1 === response.cookies.get.find(_.name == "c1").get, url)
    val bodyLines = response.body.toString.split("\n").toSeq
    val cookieLine = extractLineFromResponse("\"Cookie\"", bodyLines)
    assert(cookieLine contains ("c1=v1"), cookieLine)
  }


  textPlainGetWithQueryString(httpClient, "http://" + serverUrl + "/get?A=1&B=2")

  private def textPlainGetWithQueryString(http: Http, url: String) {
    val response = http.get(new URL(url), gzipHeaders)
    assert(200 === response.status.code, url)
    val body = response.body
    assert(APPLICATION_JSON.value === body.contentType.value, url)
    val bodyLines = response.body.toString.split("\n").toSeq
    assert("\"1\"," === extractLineFromResponse("\"A\"", bodyLines), response.body)
    assert("\"2\"" === extractLineFromResponse("\"B\"", bodyLines), response.body)
//    assert(Set("""A: 1""", """"B: 2""") === bodyLines.filter(_.startsWith("    \"")).reverse.take(2).map(_.trim).toSet, response.body)
  }

}
