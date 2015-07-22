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

package uk.co.bigbeeconsultants.http.cache

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.response.{Status, Response, BufferedResponseBuilder}
import uk.co.bigbeeconsultants.http.Config
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.header.{Cookie, Headers, MediaType, CookieJar}

class CookieCacheTest extends FunSuite {

  test("initially empty") {
    val httpStub = new HttpExecutorStub
    val cookieCache = new CookieCache(httpStub)
    assert(cookieCache.cookies.size === 0)
  }

  test("execute") {
    val cookie1 = Cookie("c1", "v1")
    val cookie2 = Cookie("c2", "v2")
    val request = Request.get("http://localhost/stuff")
    val responseBuilder = new BufferedResponseBuilder
    val expectedResponse = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", Headers.Empty, Some(CookieJar(cookie1, cookie2)))
    val httpStub = new HttpExecutorStub
    httpStub.wantedResponse = expectedResponse
    val cookieCache = new CookieCache(httpStub)
    cookieCache.execute(request, responseBuilder, Config())
    assert(cookieCache.cookies.size === 2)
  }
}
