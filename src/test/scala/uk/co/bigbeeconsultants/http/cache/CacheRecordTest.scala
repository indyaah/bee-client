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
      val record = CacheRecord(response, i)
      assert(record.lastModified === None)
      assert(record.expires === None)
      assert(record.isFirstHand)
      val currentAge = record.currentAge
      assert(currentAge < 500, currentAge)
    }
  }

  test("date header only") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val now = new HttpDateTimeInstant().toString
      val responseHeaders = Headers(DATE -> now)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      val currentAge = record.currentAge
      assert(currentAge <= 1000, currentAge)
    }
  }

  test("age and date headers") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val now = new HttpDateTimeInstant().toString
      val responseHeaders = Headers(DATE -> now, AGE -> "3")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      val currentAge = record.currentAge
      assert(2500 < currentAge && currentAge < 3500, currentAge)
      assert(!record.isFirstHand)
    }
  }

  test("already expired with expires and date headers") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val now = new HttpDateTimeInstant().toString
      val responseHeaders = Headers(DATE -> now, EXPIRES -> now)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      assert(record.isAlreadyExpired)
      assert(record.isFirstHand)
    }
  }

  test("not already expired with expires and date headers and cache-control") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val now = new HttpDateTimeInstant().toString
      val responseHeaders = Headers(DATE -> now, EXPIRES -> now, CACHE_CONTROL -> "max-age=123")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      assert(!record.isAlreadyExpired)
      assert(record.isFirstHand)
    }
  }

  test("not already expired with expires and date headers") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val now1 = new HttpDateTimeInstant()
      val now2 = new HttpDateTimeInstant(now1.seconds + 1)
      val responseHeaders = Headers(DATE -> now1.toString, EXPIRES -> now2.toString)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      assert(!record.isAlreadyExpired)
      assert(record.isFirstHand)
    }
  }

  test("max-age header with delta") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> "max-age=123")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      assert(record.maxAge === Some(123000))
    }
  }

  test("max-age header without delta") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(CACHE_CONTROL -> "no-cache")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response, i)
      assert(record.maxAge === None)
    }
  }

  test("contentLength") {
    for (i <- 0 until reps) {
      val request = Request(GET, "http://localhost/stuff")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers())
      val record = CacheRecord(response, i)
      assert(record.contentLength === 10)
    }
  }
}
