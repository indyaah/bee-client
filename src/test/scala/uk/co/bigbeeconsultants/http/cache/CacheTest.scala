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
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{Status, Response}
import uk.co.bigbeeconsultants.http.header.{Headers, MediaType}
import uk.co.bigbeeconsultants.http.header.HeaderName._

class CacheTest extends FunSuite {

  import Request._

  test("store succeeds with 200, 203, 300, 301, 410") {
    for (method <- List(GET, HEAD)) {
      val request = Request(method, "http://localhost/stuff")
      for (status <- List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)) {
        val cache = new Cache()
        val response = Response(request, status, MediaType.TEXT_PLAIN, "OK")
        cache.store(response)
        assert(cache.size === 1)
      }
    }
  }

  test("store succeeds with 404 when assume404Age is non-zero") {
    for (method <- List(GET, HEAD)) {
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S404_NotFound
      val cache = new Cache(assume404Age = 100)
      val response = Response(request, status, MediaType.TEXT_PLAIN, "OK")
      cache.store(response)
      assert(cache.size === 1)
    }
  }

  test("store ignores other methods") {
    val cache = new Cache()
    for (method <- List(POST, PUT, DELETE)) {
      val request = Request(method, "http://localhost/stuff")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK")
      cache.store(response)
      assert(cache.size === 0)
    }
  }

  test("store ignores no-cache and no-store cache-control values") {
    val cache = new Cache()
    for (value <- List("no-cache", "no-store")) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> value)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      cache.store(response)
      assert(cache.size === 0)
    }
  }
}
