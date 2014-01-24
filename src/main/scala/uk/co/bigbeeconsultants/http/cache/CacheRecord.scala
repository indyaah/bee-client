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
import uk.co.bigbeeconsultants.http.header.HttpDateTimeInstant

@deprecated("This is not yet ready for production use", "v0.25.1")
case class CacheRecord(response: Response) {

  import HttpDateTimeInstant._

  val requestTime = response.request.cacheKey.timestamp
  val responseTime = timeNowMillis
  val age: Option[Long] = response.headers.get(AGE) map (_.toNumber.toLong * 1000)
  val date: Long = response.headers.get(DATE) map (_.toDate.date.milliseconds + 500) getOrElse responseTime
  val expires: Option[Long] = response.headers.get(EXPIRES) map (_.toDate.date.milliseconds)
  val lastModified: Option[Long] = response.headers.get(LAST_MODIFIED) map (_.toDate.date.milliseconds)

  // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.2.3
  val apparentAge = math.max(responseTime - date, 0L)
  val correctedReceivedAge = if (age.isDefined) math.max(age.get, apparentAge) else apparentAge
  val responseDelay = responseTime - requestTime
  val correctedInitialAge = correctedReceivedAge + responseDelay

  def currentAge = {
    val now = System.currentTimeMillis()
    val residentTime = now - responseTime
    correctedInitialAge + residentTime
  }

  def adjustedResponse = {
    val ageHdr = AGE -> currentAge.toString
    Response(response.request, response.status, response.body, response.headers set ageHdr, response.cookies)
  }
}

