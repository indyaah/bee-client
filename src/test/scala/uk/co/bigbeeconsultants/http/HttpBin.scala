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
import util.{JSONWrapper, DumbTrustManager}

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

  implicit val config = Config(connectTimeout = 10000, readTimeout = 10000, followRedirects = false, proxy = proxy)
  var httpClient = new HttpClient(config)
  var httpBrowser = new HttpBrowser(config)

  val gzipHeaders = Headers(ACCEPT_ENCODING -> GZIP)

  private def expectHeaderIfPresent(expected: Any)(headers: Headers, name: HeaderName) {
    val hdrs = headers filter name
    if (!hdrs.isEmpty) {
      assert(hdrs(0).value == expected, hdrs(0))
    }
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
    val json = JSONWrapper(response.body.toString)
    assert(serverUrl === json.get("headers/Host").asString)
  }


  val configWithUserAgent = Config(userAgentString = Some("HttpBin-test-app"))

  private def htmlGetUserAgent(http: Http, url: String) {
    println("GET " + url)
    val response = http.get(new URL(url), gzipHeaders)
    assert(response.status.code === 200, url)
    val body = response.body
    assert(body.contentType.value === APPLICATION_JSON.value, url)
    val json = JSONWrapper(response.body.toString)
    assert("HttpBin-test-app" === json.get("user-agent").asString)
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
    assert(APPLICATION_JSON.value === response.body.contentType.value, url)
    val json = JSONWrapper(response.body.toString)
    assert(cookieC1V1 === response.cookies.get.find(_.name == "c1").get, url)
    val cookieLine = json.get("headers/Cookie").asString
    assert(cookieLine contains ("c1=v1"), cookieLine)
  }


  textPlainGetWithQueryString(httpClient, "http://" + serverUrl + "/get?A=1&B=2")

  private def textPlainGetWithQueryString(http: Http, url: String) {
    val response = http.get(new URL(url), gzipHeaders)
    assert(200 === response.status.code, url)
    assert(APPLICATION_JSON.value === response.body.contentType.value, url)
    val json = JSONWrapper(response.body.toString)
    val args = json.get("args")
    assert(2 === args.asMap.size, response.body)
    assert("1" === args.get("A").asString, response.body)
    assert("2" === args.get("B").asString, response.body)
  }


  val fredBloggs = new Credential("fred", "bloggs")
  basicAuth(httpClient, "http://" + serverUrl + "/basic-auth/fred/bloggs")
  basicAuth(httpClient, "https://" + serverUrl + "/basic-auth/fred/bloggs")
  basicAuth(httpBrowser, "http://" + serverUrl + "/basic-auth/fred/bloggs")
  basicAuth(httpBrowser, "https://" + serverUrl + "/basic-auth/fred/bloggs")

  private def basicAuth(http: Http, urlStr: String) {
    val url = new URL(urlStr)
    val response1 = http.get(url, gzipHeaders)
    assert(401 === response1.status.code, urlStr)
    val response2 = http.get(url, gzipHeaders + fredBloggs.toBasicAuthHeader)
    assert(200 === response2.status.code, urlStr)
    val json = JSONWrapper(response2.body.toString)
    assert(json.get("authenticated").asBoolean)
  }


  val realm = "me@kennethreitz.com"
  digestAuth(httpClient, "http://" + serverUrl + "/digest-auth/auth/fred/bloggs")

  private def digestAuth(http: Http, urlStr: String) {
    val registry = new AuthenticationRegistry(new CredentialSuite(Map(realm -> fredBloggs)))
    val url = new URL(urlStr)

    val response1 = http.get(url, gzipHeaders)
    assert(401 === response1.status.code, urlStr)

    response1.headers.foreach(println)
    val authHeader = registry.processResponse(response1)
    println(authHeader)

    val response2 = http.get(url, gzipHeaders + authHeader.get)
    assert(200 === response2.status.code, urlStr)
    val json = JSONWrapper(response2.body.toString)
    assert(json.get("authenticated").asBoolean)
  }


  val browserWithCreds = new HttpBrowser(config, CookieJar.empty, new CredentialSuite(Map("Fake Realm" -> fredBloggs)))
  automaticBasicAuth(browserWithCreds, "http://" + serverUrl + "/basic-auth/fred/bloggs")
  automaticBasicAuth(browserWithCreds, "https://" + serverUrl + "/basic-auth/fred/bloggs")

  private def automaticBasicAuth(http: Http, urlStr: String) {
    val url = new URL(urlStr)
    val response = http.get(url, gzipHeaders)
    assert(200 === response.status.code, urlStr)
    val json = JSONWrapper(response.body.toString)
    assert(json.get("authenticated").asBoolean)
  }

}
