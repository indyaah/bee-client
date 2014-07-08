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

import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.response.Response
import uk.co.bigbeeconsultants.http.header.{CacheControlValue, HttpDateTimeInstant}
import java.util.concurrent.atomic.AtomicInteger

private[http] case class CacheRecord(response: Response, id: Int) extends Ordered[CacheRecord] {

  import HttpDateTimeInstant._

  // Note: all `Long` durations and timestamps are in milliseconds

  /** The timestamp at which the request was constructed (milliseconds). */
  val requestTime = response.request.cacheKey.timestamp
  /** The timestamp now (milliseconds). */
  val responseTime = timeNowMillis

  private val ageHeader = response.headers.ageHdr
  private val dateHeader = response.headers.dateHdr
  private val expiresHeader = response.headers.expiresHdr
  val lastModifiedHeader = response.headers.lastModifiedHdr
  val cacheControlHeader = response.headers.cacheControlHdr
  lazy val etagHeader = response.headers.etagHdr

  /** True iff the response came directly from the origin server, not from a cache. This tests whether the age header is absent. */
  val isFirstHand = ageHeader.isEmpty

  /** True iff the expires header is set and is in the past. */
  val isAlreadyExpired = expiresHeader.isDefined && dateHeader.isDefined && cacheControlHeader.isEmpty && expiresHeader.get <= dateHeader.get

  /** The 'Age' header, if present (duration in milliseconds). */
  val age: Option[Long] = ageHeader map (_ * 1000L)
  /** The 'Date' header, if present (timestamp in milliseconds). Else the response time. */
  val date: Long = dateHeader map (_.milliseconds) getOrElse responseTime
  /** The 'Expires' header, if present (timestamp in milliseconds). */
  val expires: Option[Long] = expiresHeader map (_.milliseconds)
  /** The 'Last-Modified' header, if present (timestamp in milliseconds). */
  val lastModified: Option[Long] = lastModifiedHeader map (_.milliseconds)

  /** The max-age or s-maxage given by the 'Cache-Control' header, if present (duration in milliseconds). */
  val maxAge: Option[Long] =
    if (cacheControlHeader.isEmpty) None
    else
      cacheControlHeader.get.label match {
        case "max-age" | "s-maxage" if cacheControlHeader.get.deltaSeconds.isDefined =>
          Some(cacheControlHeader.get.deltaSeconds.get * 1000L)
        case _ =>
          None
      }

  //  /** The shared s-maxage given by the 'Cache-Control' header, if present (duration in milliseconds). */
  //  val sharedMaxAge: Option[Long] =
  //    cacheControlHeader match {
  //      case Some(CacheControlValue(_, true, "s-maxage", Some(deltaSeconds), None)) => Some(deltaSeconds * 1000)
  //      case _ => None
  //    }

  // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.2.3
  private val correctedInitialAge = {
    val apparentAge = math.max(responseTime - date, 0L)
    val correctedReceivedAge = if (age.isDefined) math.max(age.get, apparentAge) else apparentAge
    val responseDelay = responseTime - requestTime
    correctedReceivedAge + responseDelay
  }

  // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.2.4
  /** The freshness lifetime of this response (duration in milliseconds). */
  val freshnessLifetime: Long = {
    if (maxAge.isDefined) maxAge.get
    else if (expires.isDefined) expires.get - date
    else 0L
  }

  /** The timestamp (milliseconds) after which this cache record will be stale. */
  val expiresAt = (freshnessLifetime + responseTime) - correctedInitialAge

  /** The current age of the response, calculated according to RFC2616 sec13.2.3 (duration in milliseconds). */
  def currentAge: Long = {
    val now = System.currentTimeMillis()
    val residentTime = now - responseTime
    correctedInitialAge + residentTime
  }

  /** True iff the current age is less than the freshness lifetime. */
  def isFresh = freshnessLifetime > currentAge

  /** Gets a copy of the response with extra headers applied as required by RFC2616, */
  def adjustedResponse = {
    val ageHeader = AGE -> currentAge.toString
    Response(response.request, response.status, response.body, response.headers set ageHeader, response.cookies)
  }

  /**
   * Gets the length of the content as the number of bytes received. If compressed on the wire,
   * this will be the uncompressed length so will differ from the 'Content-Length' header. Because
   * 'contentLength' represents the byte array size, 'asString.length' will probably compute a different value.
   */
  def contentLength: Int = response.body.contentLength

  /** Provides ordering of records according to their remaining time to live. */
  override def compare(that: CacheRecord) = that.expiresAt.compare(this.expiresAt)
}

object CacheRecord {
  val counter = new AtomicInteger()

  def apply(response: Response) = new CacheRecord(response, counter.incrementAndGet())
}
