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

import auth.{AuthenticationRegistry, CredentialSuite, Credential}
import HttpClient._
import header.MediaType._
import header.HeaderName._
import java.net.{Proxy, URL}
import header._
import org.scalatest.Assertions
import request.RequestBody
import url.Domain
import util.JSONWrapper
import org.scalatest.exceptions.TestFailedException
import uk.co.bigbeeconsultants.http.cache.InMemoryCache

/**
 * Tests the API against httpbin.org. This is an app rather than a unit test because it would be rude to
 * hammer their site on every automatic rebuild.
 */
object HttpBinOrg extends App with Assertions {

  private val jsonSample = """{ "x": 1, "y": true }"""
  private val jsonBody = RequestBody(jsonSample, APPLICATION_JSON)
  private val tester = new Tester

  val serverUrl = "httpbin.org"

  //    val proxyAddress = new InetSocketAddress("localhost", 8888)
  //    val proxy = Some(new Proxy(Proxy.Type.HTTP, proxyAddress))
  val proxy = Proxy.NO_PROXY

  implicit val config = Config(connectTimeout = 10000, readTimeout = 10000,
    followRedirects = false, proxy = Some(proxy)).allowInsecureSSL

  var httpClient = new HttpClient(config)
  var httpBrowser = new HttpBrowser(commonConfig = config, cache = new InMemoryCache())

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)

  private def expectHeaderIfPresent(expected: Any)(headers: Headers, name: HeaderName) {
    val hdrs = headers filter name
    if (!hdrs.isEmpty) {
      tester.test(hdrs(0).value === expected, hdrs(0))
    }
  }

  headTest(httpClient, serverUrl + "/")
  headTest(httpBrowser, serverUrl + "/")

  def headTest(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      println("HEAD " + url)
      val response = http.head(url, gzipHeaders)
      tester.test(response.status.code === 200, url)
      val body = response.body
      tester.test(body.contentType.mediaType === TEXT_HTML.mediaType, url)
      expectHeaderIfPresent("gzip")(response.headers, CONTENT_ENCODING)
      tester.test(body.asString === "", url)
    }
  }


  htmlGet(httpClient, serverUrl + "/headers")
  htmlGet(httpBrowser, serverUrl + "/headers")

  private def htmlGet(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      println("GET " + url)
      val response = http.get(url, gzipHeaders)
      tester.test(response.status.code === 200, url)
      val body = response.body
      tester.test(body.contentType.mediaType === APPLICATION_JSON.mediaType, url)
      val json = JSONWrapper(response.body.asString)
      tester.test(serverUrl === json.get("headers/Host").asString, url)
    }
  }


  val configWithUserAgent = Config(userAgentString = Some("HttpBin-test-app"))
  htmlGetUserAgent(new HttpClient(configWithUserAgent), serverUrl + "/user-agent")

  private def htmlGetUserAgent(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      println("GET " + url)
      val response = http.get(url, gzipHeaders)
      tester.test(response.status.code === 200, url)
      val body = response.body
      tester.test(body.contentType.mediaType === APPLICATION_JSON.mediaType, url)
      val json = JSONWrapper(response.body.asString)
      tester.test("HttpBin-test-app" === json.get("user-agent").asString, url)
    }
  }


  textHtmlGet204(httpClient, serverUrl + "/status/204")
  textHtmlGet204(httpBrowser, serverUrl + "/status/204")

  private def textHtmlGet204(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      println("GET " + url)
      val response = http.get(url, gzipHeaders)
      tester.test(204 === response.status.code, url)
      val body = response.body
      tester.test(false === response.status.isBodyAllowed, response.status)
      tester.test(TEXT_HTML.mediaType === body.contentType.mediaType, url)
      tester.test("" === body.asString, url)
    }
  }


  val cookieC1V1 = Cookie("c1", "v1", Domain(serverUrl))
  val configFollowRedirects = Config(followRedirects = true)
  textPlainGetFollowingRedirect(new HttpClient(configFollowRedirects), serverUrl + "/redirect/1")
  textPlainGetFollowingRedirect(new HttpBrowser(configFollowRedirects, CookieJar(cookieC1V1)), serverUrl + "/redirect/1")

  private def textPlainGetFollowingRedirect(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      println("GET " + url)
      val response = http.get(url, gzipHeaders, CookieJar(cookieC1V1))
      tester.test(200 === response.status.code, url)
      tester.test(APPLICATION_JSON.mediaType === response.body.contentType.mediaType, url)
      val json = JSONWrapper(response.body.asString)
      tester.test(cookieC1V1 === response.cookies.get.find(_.name == "c1").get, url)
      val cookieLine = json.get("headers/Cookie").asString
      tester.test(cookieLine contains "c1=v1", cookieLine)
    }
  }


  textPlainGetWithQueryString(httpClient, "http://" + serverUrl + "/get?A=1&B=2")

  private def textPlainGetWithQueryString(http: Http, url: String) {
    val response = http.get(new URL(url), gzipHeaders)
    tester.test(200 === response.status.code, url)
    tester.test(APPLICATION_JSON.mediaType === response.body.contentType.mediaType, url)
    val json = JSONWrapper(response.body.asString)
    val args = json.get("args")
    tester.test(2 === args.asMap.size, response.body)
    tester.test("1" === args.get("A").asString, response.body)
    tester.test("2" === args.get("B").asString, response.body)
  }


  val fredBloggs = new Credential("fred", "bloggs")
  basicAuth(httpClient, serverUrl + "/basic-auth/fred/bloggs")
  basicAuth(httpBrowser, serverUrl + "/basic-auth/fred/bloggs")

  private def basicAuth(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      val response1 = http.get(url, gzipHeaders)
      tester.test(401 === response1.status.code, url)
      val response2 = http.get(url, gzipHeaders + fredBloggs.toBasicAuthHeader)
      tester.test(200 === response2.status.code, url)
      val json = JSONWrapper(response2.body.asString)
      tester.test(json.get("authenticated").asBoolean, url)
    }
  }


  val realm = "me@kennethreitz.com"
  //TODO digestAuth(httpClient, serverUrl + "/digest-auth/auth/fred/bloggs")

  private def digestAuth(http: Http, urlStr: String) {
    val registry = new AuthenticationRegistry(new CredentialSuite(Map(realm -> fredBloggs)))
    for (url <- httpAndHttpsUrls(urlStr)) {
      val response1 = http.get(url, gzipHeaders)
      tester.test(401 === response1.status.code, url)

      response1.headers.foreach(println)
      val authHeader = registry.processResponse(response1)
      println(authHeader)

      val response2 = http.get(url, gzipHeaders + authHeader.get)
      tester.test(200 === response2.status.code, url)
      val json = JSONWrapper(response2.body.asString)
      tester.test(json.get("authenticated").asBoolean, url)
    }
  }


  val browserWithCreds = new HttpBrowser(config, CookieJar.Empty, new CredentialSuite(Map("Fake Realm" -> fredBloggs, realm -> fredBloggs)))
  automaticAuth(browserWithCreds, serverUrl + "/basic-auth/fred/bloggs")
  automaticAuth(browserWithCreds, serverUrl + "/basic-auth/fred/bloggs")

  //TODO automaticAuth(browserWithCreds, serverUrl + "/digest-auth/auth/fred/bloggs")
  //TODO automaticAuth(browserWithCreds, serverUrl + "/digest-auth/auth/fred/bloggs")

  private def automaticAuth(http: Http, urlStr: String) {
    for (url <- httpAndHttpsUrls(urlStr)) {
      val response = http.get(url, gzipHeaders)
      assert(200 === response.status.code, url)
      val json = JSONWrapper(response.body.asString)
      assert(json.get("authenticated").asBoolean)
    }
  }

  def httpAndHttpsUrls(urlStr: String): List[URL] = {
    List(
      new URL("http://" + urlStr)
      //new URL("https://" + urlStr)
    )
  }
}

class Tester extends Assertions {
  var errors = 0

  def test(o: Option[String], clue: Any) {
    if (o.isDefined) {
      val error = new TestFailedException(clue + "\n" + o.get, 4)
      System.err.println(clue)
      error.printStackTrace()
      errors += 1
    }
  }

  def test(condition: Boolean, clue: Any) {
    if (!condition) {
      val error = new TestFailedException(clue.toString, 4)
      System.err.println(clue)
      error.printStackTrace()
      errors += 1
    }
  }

  def outcome() {
    if (errors > 0) System.err.println(errors + " errors.")
    else System.out.println("No errors.")
  }
}
