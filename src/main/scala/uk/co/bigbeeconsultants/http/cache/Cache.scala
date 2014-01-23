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

import java.util.concurrent.ConcurrentHashMap
import uk.co.bigbeeconsultants.http.response.Response
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.header.{Seconds, HttpDateTimeInstant}
import uk.co.bigbeeconsultants.http.request.Request

@deprecated("This is not yet ready for production use", "v0.25.1")
case class CacheRecord(lastModified: Seconds, expires: Seconds, response: Response)

@deprecated("This is not yet ready for production use", "v0.25.1")
class Cache {
  private val data = new ConcurrentHashMap[CacheKey, CacheRecord]()

  def lookup(request: Request): Either[Request, Response] = {
    val cacheRecord = data.get(request.cacheKey)
    if (cacheRecord == null)
      Left(request)
    else {
      val now = System.currentTimeMillis()
      Left(request) // TODO
    }
  }

  def store(response: Response) {
    if (isCacheable(response)) {
      val cacheControl = response.headers.get(CACHE_CONTROL) map (_.value)
      cacheControl match {
        case Some("private") | Some("no-cache") | Some("no-store") =>
        // cannot store this response

        case _ =>
          val now = Seconds.sinceEpoch
          val expires = response.headers.get(EXPIRES) map (_.toDate) map (_.date) getOrElse HttpDateTimeInstant.zero
          val record = CacheRecord(now, expires.instant, response)
          data.put(response.request.cacheKey, record)
      }
    }
  }

  //      val requestedAt = new HttpDateTimeInstant(response.request.cacheKey.timestamp / 1000)
  //      val now = new HttpDateTimeInstant()
  //      val age = response.headers.get(AGE) map(_.toNumber) map(_.toLong)
  //      val date = response.headers.get(DATE) map(_.toDate) map(_.date)
  //      val expires = response.headers.get(EXPIRES) map(_.toDate) map(_.date)
  //      val lastModified = response.headers.get(LAST_MODIFIED) map(_.toDate) map(_.date)
  //
  //      val dateDelta = date map(now - _)
  //      val dateDelta0 = if (dateDelta.isDefined && dateDelta.get < 0) Some(0) else dateDelta
  //      val correctedReceivedAge = maxo(dateDelta0, age)
  //      val correctedInitialAge = maxo(correctedReceivedAge, Some(now - requestedAt))

  private def isCacheable(response: Response) =
    (response.request.method == Request.GET ||
      response.request.method == Request.HEAD) &&
      response.body.isBuffered &&
      cacheableStatusCodes.contains(response.status.code)

  private val cacheableStatusCodes = Set(200, 203, 300, 301, 410)

  private def maxo(a: Option[Long], b: Option[Long]): Option[Long] = {
    if (a.isDefined && b.isDefined) Some(math.max(a.get, b.get))
    else if (a.isDefined) a
    else if (b.isDefined) b
    else None
  }
}
