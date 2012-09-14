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
import uk.co.bigbeeconsultants.http.request._
import uk.co.bigbeeconsultants.http.header.{Domain, Cookie, CookieJar, Headers}
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.HttpClient
import java.io.ByteArrayInputStream
import java.net.URL

class ResponseBuilderTest extends FunSuite with ShouldMatchers {

  test("buffer size should be chosen using the content-length when it is available") {
    val headers = Headers(CONTENT_LENGTH -> "1234")
    val builder = new BufferedResponseBuilder
    val size = builder.bufferSize(headers)
    size should be(1234)
  }

  test("buffer size should be defaulted when the content-length is not available") {
    val headers = Headers.empty
    val builder = new BufferedResponseBuilder
    val size = builder.bufferSize(headers)
    size should be(BufferedResponseBuilder.DefaultBufferSize)
  }

  test("conditionalUrl should return the request's URL for a successful GET request") {
    val url = new URL("http://localhost/1234")
    val request = Request.get(url)
    val builder = new BufferedResponseBuilder
    val result = builder.conditionalUrl(request, Status.S200_OK)
    result.get should be(url)
  }

  test("conditionalUrl should not return the request's URL for an unsuccessful GET request") {
    val url = new URL("http://localhost/1234")
    val request = Request.get(url)
    val builder = new BufferedResponseBuilder
    val result = builder.conditionalUrl(request, Status.S400_BadRequest)
    result should be(None)
  }

  test("conditionalUrl should not return the request's URL for a successful POST request") {
    val url = new URL("http://localhost/1234")
    val request = Request.post(url, None)
    val builder = new BufferedResponseBuilder
    val result = builder.conditionalUrl(request, Status.S200_OK)
    result should be(None)
  }

  test("BufferedResponseBuilder should capture response data correctly") {
    val builder = new BufferedResponseBuilder
    builder.response should be(None)

    val s = """Hello world]"""
    val bytes = s.getBytes(HttpClient.UTF8)
    val bais = new ByteArrayInputStream(bytes)

    val request = Request.get("http://localhost/foo/bar")
    val headers = Headers(HOST -> "localhost")
    val cookies = Some(CookieJar(Cookie("n", "v", Domain.localhost)))
    builder.captureResponse(request, Status.S200_OK, Some(TEXT_PLAIN), headers, cookies, bais)

    val response = builder.response.get
    response.request should be(request)
    response.status should be(Status.S200_OK)
    response.headers should be(headers)
    response.cookies should be(cookies)
    response.body.asString should be(s)
  }

}
