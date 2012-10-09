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

package uk.co.bigbeeconsultants.http.response

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import org.mockito.Matchers._
import uk.co.bigbeeconsultants.http.request._
import uk.co.bigbeeconsultants.http.header.{MediaType, Cookie, CookieJar, Headers}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.{HttpClient, Config}
import uk.co.bigbeeconsultants.http.response.RedirectionLogic._

class RedirectionLogicTest extends FunSuite with ShouldMatchers {

  var cfgFollow = Config(followRedirects = true)

  test("determineRedirect should return None when the status is 2xx") {
    val request = Request.get("http://localhost/foo/bar")
    for (s <- 200 to 299) {
      val redirect = determineRedirect(Config(), request, Status(s), Nil, None)
      redirect should be(None)
    }
  }

  test("determineRedirect should return None when the status is 4xx") {
    val request = Request.get("http://localhost/foo/bar")
    for (s <- 400 to 499) {
      val redirect = determineRedirect(Config(), request, Status(s), Nil, None)
      redirect should be(None)
    }
  }

  test("determineRedirect should return None when the status is 5xx") {
    val request = Request.get("http://localhost/foo/bar")
    for (s <- 500 to 599) {
      val redirect = determineRedirect(Config(), request, Status(s), Nil, None)
      redirect should be(None)
    }
  }

  test("determineRedirect should return Some(GET) when the status is 303") {
    val requestHeaders = Headers(ACCEPT_LANGUAGE -> "fr")
    val requestBody = Some(RequestBody("hello", MediaType.TEXT_PLAIN))
    val request = Request.post("http://localhost/foo/bar", requestBody, requestHeaders)
    val responseCookies = Some(CookieJar(Cookie("x", "1")))
    val headers = Headers(LOCATION -> "/go/there")
    val redirect = determineRedirect(cfgFollow, request, Status.S303_SeeOther, headers, responseCookies)
    redirect.get.method should be("GET")
    redirect.get.url.toString should be("http://localhost/go/there")
    redirect.get.headers should be(requestHeaders)
    redirect.get.cookies should be(responseCookies)
  }

  test("determineRedirect should return Some(POST) when the status is 307") {
    val requestHeaders = Headers(ACCEPT_LANGUAGE -> "fr")
    val request = Request.get("http://localhost/foo/bar", requestHeaders)
    val responseCookies = Some(CookieJar(Cookie("x", "1")))
    val headers = Headers(LOCATION -> "/go/there")
    val redirect = determineRedirect(cfgFollow, request, Status.S307_MovedTemporarily, headers, responseCookies)
    redirect.get.method should be("GET")
    redirect.get.url.toString should be("http://localhost/go/there")
    redirect.get.headers should be(requestHeaders)
    redirect.get.cookies should be(responseCookies)
  }

  test("determineRedirect should return None when the status is 303 but no location header is received") {
    val request = Request.get("http://localhost/foo/bar")
    val responseCookies = CookieJar(Cookie("x", "1"))
    val redirect = determineRedirect(cfgFollow, request, Status.S303_SeeOther, Nil, Some(responseCookies))
    redirect should be(None)
  }

  test("determineRedirect should return None when the status is 307 but no location header is received") {
    val request = Request.get("http://localhost/foo/bar")
    val responseCookies = CookieJar(Cookie("x", "1"))
    val redirect = determineRedirect(cfgFollow, request, Status.S307_MovedTemporarily, Nil, Some(responseCookies))
    redirect should be(None)
  }

  test("doExecute should terminate cleanly for no redirection when disabled") {
    val request = Request.get("http://localhost/foo/bar")
    val httpClient = mock(classOf[HttpClient])
    val config = Config(followRedirects = false)
    when (httpClient.commonConfig) thenReturn config
    when (httpClient.doExecute(any(), any(), any())) thenReturn None
    val remainingTries = doExecute(httpClient, request, new BufferedResponseBuilder, config)
    remainingTries should be(0)
  }

  test("doExecute should terminate cleanly for a redirection when disabled") {
    val request = Request.get("http://localhost/foo/bar")
    val httpClient = mock(classOf[HttpClient])
    val config = Config(followRedirects = false)
    when (httpClient.commonConfig) thenReturn config
    when (httpClient.doExecute(any(), any(), any())) thenReturn Some(request)
    val remainingTries = doExecute(httpClient, request, new BufferedResponseBuilder, config)
    remainingTries should be(0)
  }

  test("doExecute should terminate cleanly for one redirection when enabled") {
    val request = Request.get("http://localhost/foo/bar")
    val httpClient = mock(classOf[HttpClient])
    val config = Config(followRedirects = true, maxRedirects = 10)
    when (httpClient.commonConfig) thenReturn config
    when (httpClient.doExecute(any(), any(), any())) thenReturn Some(request) thenReturn None
    val remainingTries = doExecute(httpClient, request, new BufferedResponseBuilder, config)
    remainingTries should be(8)
  }

  test("doExecute should die if maxRedirects is exceeded when enabled") {
    val request = Request.get("http://localhost/foo/bar")
    val httpClient = mock(classOf[HttpClient])
    val config = Config(followRedirects = true, maxRedirects = 10)
    when (httpClient.commonConfig) thenReturn config
    when (httpClient.doExecute(any(), any(), any())) thenReturn Some(request)
    intercept[IllegalStateException] {
      doExecute(httpClient, request, new BufferedResponseBuilder, config)
    }
  }
}
