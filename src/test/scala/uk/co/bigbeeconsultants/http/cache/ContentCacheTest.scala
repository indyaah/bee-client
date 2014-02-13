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
import uk.co.bigbeeconsultants.http.response._
import uk.co.bigbeeconsultants.http.header._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.util.{Duration, DiagnosticTimer}
import uk.co.bigbeeconsultants.http.response.StringResponseBody
import uk.co.bigbeeconsultants.http.header.HttpDateTimeInstant

@RunWith(classOf[JUnitRunner])
class ContentCacheTest extends FunSuite {

  import Request._

  val getAndHead = List(GET, HEAD)
  val supportedCodes = List(Status.S200_OK, Status.S203_NotAuthoritative, Status.S300_MultipleChoice, Status.S301_MovedPermanently, Status.S410_Gone)
  val text1000 = "123456789\n" * 100
  val emptyResponseBody = new EmptyResponseBody(MediaType.TEXT_PLAIN)
  val okResponseBody = new StringResponseBody("OK", MediaType.TEXT_PLAIN)

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
      val response = new Response(request, status, emptyResponseBody, Headers.Empty, None)
      val responseBuilder = new BufferedResponseBuilder
      httpStub.wantedResponse = response
      cache.execute(request, responseBuilder, config)
      assert(cache.cacheSize === 1)
    }
  }

  test("store succeeds with 503 when expires is set") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0, assume404Age = 100)
    val cache = new ContentCache(httpStub, cacheConfig)
    val now = new HttpDateTimeInstant
    val now60 = now + 60
    val headers = Headers(DATE -> now, EXPIRES -> now60)

    for (method <- getAndHead) {
      cache.clearCache()
      val request = Request(method, "http://localhost/stuff")
      val status = Status.S503_ServiceUnavailable
      val response = new Response(request, status, emptyResponseBody, headers, None)
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

  test("store ignores 206 partial content") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    for (method <- getAndHead) {
      val request = Request(method, "http://localhost/stuff")
      val response = Response(request, Status.S206_PartialContent, MediaType.TEXT_PLAIN, "OK")
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

  test("store allows other cache-control values") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val request = Request(GET, "http://localhost/stuff")
    val responseHeaders = Headers(CACHE_CONTROL -> "private")
    val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
    val responseBuilder = new BufferedResponseBuilder
    httpStub.wantedResponse = response
    cache.execute(request, responseBuilder, config)
    assert(cache.cacheSize === 1)
  }

  test("lookup gets matching entries from an almost-empty store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val now = new HttpDateTimeInstant
    val now60 = now + 60
    val headers = Headers(DATE -> now, EXPIRES -> now60)

    for (method <- getAndHead) {
      val request1 = Request(method, "http://localhost/stuff")
      for (status <- supportedCodes) {
        cache.clearCache()
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
        val responseBuilder = new BufferedResponseBuilder
        httpStub.wantedResponse = response
        cache.execute(request1, responseBuilder, config)
        assert(cache.cacheSize === 1)
        assert(!responseBuilder.response.get.headers.contains(AGE))

        val request2 = Request(method, "http://localhost/stuff")
        cache.execute(request2, responseBuilder, config)
        assert(cache.cacheSize === 1)
        assert(responseBuilder.response.get.headers(AGE) === AGE -> "0")
      }
    }
  }

  test("lookup gets matching entries from a well-filled store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val now = new HttpDateTimeInstant
    val now60 = now + 60
    val headers = Headers(DATE -> now, EXPIRES -> now60)
    val responseBuilder = new BufferedResponseBuilder

    for (method <- getAndHead) {
      for (status <- supportedCodes) {
        val request1 = Request(method, "http://localhost/stuff" + status.code)
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
        httpStub.wantedResponse = response
        cache.execute(request1, responseBuilder, config)
        assert(!responseBuilder.response.get.headers.contains(AGE))
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
    val now = new HttpDateTimeInstant
    val now60 = now + 60
    val headers = Headers(DATE -> now, EXPIRES -> now60)
    val n = 1000
    val loops = getAndHead.size * supportedCodes.size * n
    var duration1 = Duration.Zero
    var duration2 = Duration.Zero

    val request = Request.get("http://localhost/stuff")
    for (status <- supportedCodes) {
      val response = Response(request, status, MediaType.TEXT_PLAIN, "OK", headers)
      for (i <- 1 to n) {
        cache.clearCache()
        val responseBuilder = new BufferedResponseBuilder
        httpStub.wantedResponse = response
        val dt1 = new DiagnosticTimer
        cache.execute(request, responseBuilder, config)
        duration1 += dt1.duration

        val dt2 = new DiagnosticTimer
        cache.execute(request, responseBuilder, config)
        duration2 += dt2.duration
      }
    }

    val d1 = duration1 / loops
    val d2 = duration2 / loops
    println("Clear, store, lookup took " + d1 + " (miss) and " + d2 + " (hit) per loop")
  }

  test("benchmark - lookup gets matching entries from a well-filled store") {
    val config = Config()
    val httpStub = new HttpExecutorStub
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val cache = new ContentCache(httpStub, cacheConfig)
    val now = new HttpDateTimeInstant
    val now60 = now + 60
    val headers = Headers(DATE -> now, EXPIRES -> now60)
    val n = 1000
    val loops = getAndHead.size * supportedCodes.size * n
    val responseBuilder = new BufferedResponseBuilder
    var duration1 = Duration.Zero
    var duration2 = Duration.Zero

    for (i <- 1 to n) {
      val url = "http://localhost/stuff" + i
      val request1 = Request.get(url)
      for (status <- supportedCodes) {
        val response = Response(request1, status, MediaType.TEXT_PLAIN, "OK", headers)
        httpStub.wantedResponse = response
        val dt1 = new DiagnosticTimer
        cache.execute(request1, responseBuilder, config)
        duration1 += dt1.duration
      }
    }

    for (i <- 1 to n) {
      val url = "http://localhost/stuff" + i
      val request2 = Request.get(url)
      for (status <- supportedCodes) {
        val dt2 = new DiagnosticTimer
        cache.execute(request2, responseBuilder, config)
        duration2 += dt2.duration
        assert(responseBuilder.response.get.headers.contains(AGE))
      }
    }
    val d1 = duration1 / loops
    val d2 = duration2 / loops
    println("Store took " + d1 + " and lookup took " + d2 + " per loop (" + loops + ")")
  }

  test("lookup revalidates 304 entries with etag") {
    val config = Config()
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val now = new HttpDateTimeInstant
    val etag = new EntityTag("12345", false)
    val headers = Headers(DATE -> now, ETAG -> etag)

    val httpStub = new HttpExecutorStub
    val cache = new ContentCache(httpStub, cacheConfig)
    val request = Request.get("http://localhost/stuff")
    val response200 = new Response(request, Status.S200_OK, okResponseBody, headers, None)
    val responseBuilder200 = new BufferedResponseBuilder
    httpStub.wantedResponse = response200
    cache.execute(request, responseBuilder200, config)
    assert(cache.cacheSize === 1)
    assert(!responseBuilder200.response.get.headers.contains(AGE))

    val response304 = new Response(request, Status.S304_NotModified, emptyResponseBody, headers, None)
    val responseBuilder304 = new BufferedResponseBuilder
    httpStub.wantedResponse = response304
    cache.execute(request, responseBuilder304, config)
    assert(cache.cacheSize === 1)
    assert(httpStub.actualRequest.headers(IF_NONE_MATCH).value === "\"12345\"")
    val actualResponse = responseBuilder304.response.get
    assert(!responseBuilder304.response.get.headers.contains(AGE))
    assert(actualResponse.status === Status.S200_OK)
    assert(actualResponse.body.asString === "OK")
  }

  test("lookup revalidates 304 entries with newer last-modified") {
    val config = Config()
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val now = new HttpDateTimeInstant
    val now10 = now + 10
    val now20 = now + 20
    val headers = Headers(DATE -> now)

    val httpStub = new HttpExecutorStub
    val cache = new ContentCache(httpStub, cacheConfig)
    val request = Request.get("http://localhost/stuff")
    val response200 = new Response(request, Status.S200_OK, okResponseBody, headers + (LAST_MODIFIED -> now10), None)
    val responseBuilder200 = new BufferedResponseBuilder
    httpStub.wantedResponse = response200
    cache.execute(request, responseBuilder200, config)
    assert(cache.cacheSize === 1)
    assert(!responseBuilder200.response.get.headers.contains(AGE))

    val response304 = new Response(request, Status.S304_NotModified, emptyResponseBody, headers + (LAST_MODIFIED -> now20), None)
    val responseBuilder304 = new BufferedResponseBuilder
    httpStub.wantedResponse = response304
    cache.execute(request, responseBuilder304, config)
    assert(cache.cacheSize === 1)
    assert(httpStub.actualRequest.headers.get(IF_MODIFIED_SINCE).get.value === now10.toString)
    val actualResponse = responseBuilder304.response.get
    assert(!responseBuilder304.response.get.headers.contains(AGE))
    assert(actualResponse.status === Status.S200_OK)
    assert(actualResponse.headers(LAST_MODIFIED).toDate.get === now20)
    assert(actualResponse.body.asString === "OK")
  }

  test("lookup gracefully handles missing response when revalidating") {
    val config = Config()
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 0)
    val now = new HttpDateTimeInstant
    val now10 = now + 10
    val headers = Headers(DATE -> now)

    val httpStub = new HttpExecutorStub
    val cache = new ContentCache(httpStub, cacheConfig)
    val request = Request.get("http://localhost/stuff")
    val response200 = new Response(request, Status.S200_OK, okResponseBody, headers + (LAST_MODIFIED -> now10), None)
    val responseBuilder200 = new BufferedResponseBuilder
    httpStub.wantedResponse = response200
    cache.execute(request, responseBuilder200, config)
    assert(cache.cacheSize === 1)
    assert(!responseBuilder200.response.get.headers.contains(AGE))

    val responseBuilder304 = new BufferedResponseBuilder
    httpStub.wantedResponse = null
    cache.execute(request, responseBuilder304, config)
    assert(cache.cacheSize === 1)
    assert(httpStub.actualRequest.headers.get(IF_MODIFIED_SINCE).get.value === now10.toString)
    val actualResponse = responseBuilder304.response.get
    assert(!responseBuilder304.response.get.headers.contains(AGE))
    assert(actualResponse.status === Status.S200_OK)
    assert(actualResponse.headers(LAST_MODIFIED).toDate.get === now10)
    assert(actualResponse.body.asString === "OK")
  }

  test("lookup hits the cache for tiny entries with age (via expires) greater than ageThreshold") {
    val config = Config()
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 1000, ageThreshold = 60)
    val now = new HttpDateTimeInstant
    val etag = new EntityTag("12345", false)
    val now120 = now + 120
    val headers = Headers(DATE -> now, ETAG -> etag, EXPIRES -> now120)

    val httpStub = new HttpExecutorStub
    val cache = new ContentCache(httpStub, cacheConfig)
    val request = Request.get("http://localhost/stuff")
    val response1 = new Response(request, Status.S200_OK, okResponseBody, headers, None)
    val responseBuilder = new BufferedResponseBuilder
    httpStub.wantedResponse = response1
    cache.execute(request, responseBuilder, config)
    assert(cache.cacheSize === 1)
    assert(!responseBuilder.response.get.headers.contains(AGE))

    httpStub.wantedResponse = null
    cache.execute(request, responseBuilder, config)
    assert(cache.cacheSize === 1)
    val actualResponse = responseBuilder.response.get
    assert(responseBuilder.response.get.headers(AGE) === AGE -> "0")
    assert(actualResponse.status === Status.S200_OK)
    assert(actualResponse.body.asString === "OK")
  }

  test("lookup hits the cache for tiny entries with age (via maxAge) greater than ageThreshold") {
    val config = Config()
    val cacheConfig = CacheConfig(enabled = true, minContentLength = 1000, ageThreshold = 60)
    val now = new HttpDateTimeInstant
    val etag = new EntityTag("12345", false)
    val now120 = now + 120
    val headers = Headers(DATE -> now, ETAG -> etag, CACHE_CONTROL -> CacheControlValue.maxAge(120))

    val httpStub = new HttpExecutorStub
    val cache = new ContentCache(httpStub, cacheConfig)
    val request = Request.get("http://localhost/stuff")
    val response1 = new Response(request, Status.S200_OK, okResponseBody, headers, None)
    val responseBuilder = new BufferedResponseBuilder
    httpStub.wantedResponse = response1
    cache.execute(request, responseBuilder, config)
    assert(cache.cacheSize === 1)
    assert(!responseBuilder.response.get.headers.contains(AGE))

    httpStub.wantedResponse = null
    cache.execute(request, responseBuilder, config)
    assert(cache.cacheSize === 1)
    val actualResponse = responseBuilder.response.get
    assert(responseBuilder.response.get.headers(AGE) === AGE -> "0")
    assert(actualResponse.status === Status.S200_OK)
    assert(actualResponse.body.asString === "OK")
  }

}
