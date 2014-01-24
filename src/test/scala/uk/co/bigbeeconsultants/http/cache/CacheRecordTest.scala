package uk.co.bigbeeconsultants.http.cache

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{Status, Response}
import uk.co.bigbeeconsultants.http.header.{HttpDateTimeInstant, Headers, MediaType}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.request.Request._

class CacheRecordTest extends FunSuite {

  test("no headers") {
    for (i <- 0 until 1000) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers()
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      assert(record.apparentAge === 0)
      val currentAge = record.currentAge
      assert(currentAge < 400, currentAge)
    }
  }

  test("date header only") {
    for (i <- 0 until 1000) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(DATE -> new HttpDateTimeInstant().toString)
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      val currentAge = record.currentAge
      assert(currentAge <= 1000, currentAge)
    }
  }

  test("age and date headers") {
    for (i <- 0 until 1000) {
      val request = Request(GET, "http://localhost/stuff")
      val responseHeaders = Headers(DATE -> new HttpDateTimeInstant().toString, AGE -> "3")
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "OK", responseHeaders)
      val record = CacheRecord(response)
      val currentAge = record.currentAge
      assert(2990 < currentAge && currentAge < 3010, currentAge)
    }
  }
}
