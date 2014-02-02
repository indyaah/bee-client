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
import uk.co.bigbeeconsultants.http.header.HttpDateTimeInstant
import uk.co.bigbeeconsultants.http.util.DiagnosticTimer
import uk.co.bigbeeconsultants.http.util.Bytes._

class CacheTest extends FunSuite {

  import Request._

  test("store succeeds with 200, 203, 300, 301, 410") {
    val cache = new InMemoryCache()
    for (method <- List(GET, HEAD)) {
      val request = Request(method, "http://localhost/stuff")
      for (status <- List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)) {
        cache.clear()
        val response = Response(request, status, MediaType.TEXT_PLAIN, "OK")
        cache.store(response)
        assert(cache.size === 1)
      }
    }
  }

  test("store succeeds with 404 when assume404Age is non-zero") {
    val cache = new InMemoryCache(assume404Age = 100)
    for (method <- List(GET, HEAD)) {
      cache.clear()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S404_NotFound
      val response = Response(request, status, MediaType.TEXT_PLAIN, "OK")
      cache.store(response)
      assert(cache.size === 1)
    }
  }

  test("store ignores other methods") {
    val cache = new InMemoryCache()
    for (method <- List(POST, PUT, DELETE)) {
      val request = Request(method, "http://localhost/stuff")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK")
      cache.store(response)
      assert(cache.size === 0)
    }
  }

  test("store ignores no-cache and no-store cache-control values") {
    val cache = new InMemoryCache()
    for (value <- List("no-cache", "no-store")) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> value)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      cache.store(response)
      assert(cache.size === 0)
    }
  }

  test("lookup gets matching entries from an almost-empty store") {
    val cache = new InMemoryCache()
    for (method <- List(GET, HEAD)) {
      val request1 = Request(method, "http://localhost/stuff")
      for (status <- List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)) {
        cache.clear()
        val nowPlus60 = new HttpDateTimeInstant(HttpDateTimeInstant.timeNowSec + 60)
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK",
          Headers(EXPIRES -> nowPlus60))
        cache.store(response)
        assert(cache.size === 1)
        val request2 = Request(method, "http://localhost/stuff")
        val result = cache.lookup(request2)
        assert(result match {
          case CacheFresh(Response(request1, _, _, _, _)) => true
          case _ => false
        }, result.toString)
      }
    }
  }

  test("lookup gets matching entries from a well-filled store") {
    val cache = new InMemoryCache(10 * MiB)
    val n = 20
    val dt1 = new DiagnosticTimer
    for (i <- 1 to n) {
      for (method <- List(GET, HEAD)) {
        val request1 = Request(method, "http://localhost/stuff" + i)
        for (status <- List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)) {
          val nowPlus60 = new HttpDateTimeInstant(HttpDateTimeInstant.timeNowSec + 60)
          val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK",
            Headers(EXPIRES -> nowPlus60))
          cache.store(response)
        }
      }
    }
    val d1 = dt1.duration
    val dt2 = new DiagnosticTimer
    for (i <- 1 to n) {
      for (method <- List(GET, HEAD)) {
        for (status <- List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)) {
          val request2 = Request(method, "http://localhost/stuff" + i)
          val result = cache.lookup(request2)
          assert(result match {
            case CacheFresh(Response(request2, _, _, _, _)) => true
            case _ => false
          }, result.toString)
        }
      }
    }
    val d2 = dt2.duration
    println("Took " + d1 + " and " + d2)
  }
}
