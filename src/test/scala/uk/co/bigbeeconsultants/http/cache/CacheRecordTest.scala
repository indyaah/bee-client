package uk.co.bigbeeconsultants.http.cache

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{Status, Response}
import uk.co.bigbeeconsultants.http.header.{CacheControlValue, HttpDateTimeInstant, Headers, MediaType}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.request.Request._

class CacheRecordTest extends FunSuite {

  val reps = 10 // increase for soak testing

  test("no headers") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers()
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      assert(record.lastModified === None)
      assert(record.expires === None)
      assert(record.apparentAge === 0)
      val currentAge = record.currentAge
      assert(currentAge < 400, currentAge)
    }
  }

  test("date header only") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(DATE -> new HttpDateTimeInstant().toString)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      val currentAge = record.currentAge
      assert(currentAge <= 1000, currentAge)
    }
  }

  test("age and date headers") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(DATE -> new HttpDateTimeInstant().toString, AGE -> "3")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      val currentAge = record.currentAge
      assert(2950 < currentAge && currentAge < 3050, currentAge)
    }
  }

  test("max-age header with delta") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> "max-age=123")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      assert(record.maxAge === Some(123))
    }
  }

  test("max-age header without delta") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> "no-cache")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      assert(record.maxAge === None)
    }
  }

  test("contentLength") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers())
      val record = CacheRecord(response)
      assert(record.contentLength === 10)
    }
  }
}
