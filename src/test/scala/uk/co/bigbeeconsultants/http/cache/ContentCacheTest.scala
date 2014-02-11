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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{BufferedResponseBuilder, Status, Response}
import uk.co.bigbeeconsultants.http.header.{Headers, MediaType, HttpDateTimeInstant}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.util.DiagnosticTimer

@RunWith(classOf[JUnitRunner])
class ContentCacheTest extends FunSuite {

  import Request._

  val getAndHead = List(GET, HEAD)
  val supportedCodes = List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)
  val text1000 = "123456789\n" * 100

  test("when minContentLength is zero, store succeeds with 200, 203, 300, 301, 410") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      val request = Request(method, "http://localhost/stuff")
      for (status <- supportedCodes) {
        cache.clearCache()
        val response = Response(request, status, MediaType.TEXT_PLAIN, "")
        val responseBuilder = new BufferedResponseBuilder
        httpStub.wantedResponse = response
        cache.execute(request, responseBuilder, config)
        assert(cache.cacheSize === 1)
      }
    }
  }

  test("store succeeds with 404 when assume404Age is non-zero") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0, assume404Age = 100)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      cache.clearCache()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S404_NotFound
      val response = Response(request, status, MediaType.TEXT_PLAIN, "")
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 1)
    }
  }

  test("store ignores actual content length less than the threshold") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = text1000.length + 1)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      cache.clearCache()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S200_OK
      val response = Response(request, status, MediaType.TEXT_PLAIN, text1000)
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 0)
    }
  }

  test("store succeeds for actual content length >= the threshold") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = text1000.length)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      cache.clearCache()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S200_OK
      val response = Response(request, status, MediaType.TEXT_PLAIN, text1000)
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 1)
    }
  }

  test("store ignores header-set content length less than the threshold") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 500)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      cache.clearCache()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S200_OK
      val response = Response(request, status, MediaType.TEXT_PLAIN, text1000, Headers(CONTENT_LENGTH -> "499"))
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 0)
    }
  }

  test("store succeeds for header-set content length >= the threshold") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 500)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      cache.clearCache()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S200_OK
      val response = Response(request, status, MediaType.TEXT_PLAIN, text1000, Headers(CONTENT_LENGTH -> "500"))
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 1)
    }
  }

  test("store ignores other methods") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- List(POST, PUT, DELETE)) {
      val request = Request(method, "http://localhost/stuff")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK")
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 0)
    }
  }

  test("store ignores no-cache and no-store cache-control values") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (value <- List("no-cache", "no-store")) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> value)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 0)
    }
  }

  test("lookup gets matching entries from an almost-empty store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val nowPlus60 = new HttpDateTimeInstant(HttpDateTimeInstant.timeNowSec + 60)
    val headers = Headers(EXPIRES -> nowPlus60)
    for (method <- getAndHead) {
      val request1 = Request(method, "http://localhost/stuff")
      for (status <- supportedCodes) {
        cache.clearCache()
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
        val responseBuilder = new BufferedResponseBuilder
        httpStub.wantedResponse = response
        cache.execute(request1, responseBuilder, config)
        assert(cache.cacheSize === 1)
        assert(responseBuilder.response.get.headers.get(AGE) === None)

        val request2 = Request(method, "http://localhost/stuff")
        cache.execute(request2, responseBuilder, config)
        assert(cache.cacheSize === 1)
        assert(responseBuilder.response.get.headers.get(AGE) === Some(AGE -> "0"))
      }
    }
  }

  test("lookup gets matching entries from a well-filled store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val nowPlus60 = new HttpDateTimeInstant(HttpDateTimeInstant.timeNowSec + 60)
    val headers = Headers(EXPIRES -> nowPlus60)
    val responseBuilder = new BufferedResponseBuilder
    for (method <- getAndHead) {
      for (status <- supportedCodes) {
        val request1 = Request(method, "http://localhost/stuff" + status.code)
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
        httpStub.wantedResponse = response
        cache.execute(request1, responseBuilder, config)
        assert(responseBuilder.response.get.headers.get(AGE) === None)
      }
    }
    for (method <- getAndHead) {
      for (status <- supportedCodes) {
        val request2 = Request(method, "http://localhost/stuff" + status.code)
        cache.execute(request2, responseBuilder, config)
        assert(responseBuilder.response.get.headers.contains(AGE))
      }
    }
  }

  test("benchmark - lookup gets matching entries from an almost-empty store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val nowPlus60 = new HttpDateTimeInstant(HttpDateTimeInstant.timeNowSec + 60)
    val headers = Headers(EXPIRES -> nowPlus60)
    val n = 100
    val loops = getAndHead.size * supportedCodes.size * n
    val dt = new DiagnosticTimer
    for (method <- getAndHead) {
      val request1 = Request(method, "http://localhost/stuff")
      for (status <- supportedCodes) {
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
        for (i <- 1 to n) {
          cache.clearCache()
          val responseBuilder = new BufferedResponseBuilder
          httpStub.wantedResponse = response
          cache.execute(request1, responseBuilder, config)

          val request2 = request1
          cache.execute(request2, responseBuilder, config)
        }
      }
    }
    val d = dt.duration / loops
    println("Clear, store, lookup took " + d + " per loop")
  }

  test("benchmark - lookup gets matching entries from a well-filled store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val nowPlus60 = new HttpDateTimeInstant(HttpDateTimeInstant.timeNowSec + 60)
    val headers = Headers(EXPIRES -> nowPlus60)
    val n = 100
    val loops = getAndHead.size * supportedCodes.size * n
    val responseBuilder = new BufferedResponseBuilder
    val dt1 = new DiagnosticTimer
    for (i <- 1 to n) {
      val url = "http://localhost/stuff" + i
      for (method <- getAndHead) {
        val request1 = Request(method, url)
        for (status <- supportedCodes) {
          val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
          httpStub.wantedResponse = response
          cache.execute(request1, responseBuilder, config)
        }
      }
    }
    val d1 = dt1.duration / loops
    val dt2 = new DiagnosticTimer
    for (i <- 1 to n) {
      val url = "http://localhost/stuff" + i
      for (method <- getAndHead) {
        val request2 = Request(method, url)
        for (status <- supportedCodes) {
          cache.execute(request2, responseBuilder, config)
          assert(responseBuilder.response.get.headers.contains(AGE))
        }
      }
    }
    val d2 = dt2.duration / loops
    println("Store took " + d1 + " and lookup took " + d2 + " per loop (" + loops + ")")
  }
}
