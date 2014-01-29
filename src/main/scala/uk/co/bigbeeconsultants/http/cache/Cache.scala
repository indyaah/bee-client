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

import uk.co.bigbeeconsultants.http.response.Response
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.header.WarningValue
import uk.co.bigbeeconsultants.http.util.Bytes._

/**
 * Holds an HTTP content cache. Outbound requests are checked using `lookup`, which either returns a cached response
 * or provides an altered request to pass on to the origin server. All responses are offered to the cache via the
 * `store` method.
 *
 * The cache is *not* persistent: every time the HTTP client is started, any cache will start off empty.
 *
 * @param maxContentSize set an upper limit on the size of the cache, in terms of the total number of bytes in the
 *                       unencoded content lengths of all the cached responses. The default value is 10,000,000 bytes.
 * @param assume404Age provides optional caching for 404 responses - these are not normally cached but can therefore
 *                     be a pain. Provide an assumed age (in seconds) and all 404 responses will be stored in the
 *                     cache as if the response had contained that age in a header. Zero disables this feature and is
 *                     the default value.
 */
@deprecated("This is not yet ready for production use", "v0.25.1")
class Cache(maxContentSize: Long = 10 * MiB, assume404Age: Int = 0) {
  require(maxContentSize >= 0, "maxContentSize must be non-negative")
  require(assume404Age >= 0, "assume404Age must be non-negative")

  private val data = new CacheStore(maxContentSize)

  private[cache] def size = data.size

  def clear() {
    data.clear()
  }

  def lookup(request: Request): Either[Request, Response] = {
    val requestCacheControl = request.headers.get(CACHE_CONTROL) map (_.toCacheControlValue)
    val cacheRecord = data.get(request.cacheKey)
    if (cacheRecord == null)
      Left(request)
    else {
      val now = System.currentTimeMillis()
      // TODO set Age header
      Right(null) // TODO
    }
  }

  def store(response: Response) {
    if (maxContentSize > 0 && isCacheable(response)) {
      response.status.code match {
        case 200 | 203 | 300 | 301 | 410 => offerToCache(response)
        case 404 if assume404Age > 0 =>
          val age = AGE -> assume404Age
          val warning = WARNING -> WarningValue(110, "", "Stale content")
          val modHeaders = response.headers.set(age).set(warning)
          offerToCache(response.copy(headers = modHeaders))
        case _ => // cannot store this response
      }
    }
  }

  private def offerToCache(response: Response) {
    val cacheControl = response.headers.get(CACHE_CONTROL) map (_.toCacheControlValue)
    if (cacheControl.isDefined)
      cacheControl.get.label match {
        case "no-cache" | "no-store" => // cannot store this response
        case _ => data.put(response)
      } else data.put(response)
  }

  private def isCacheable(response: Response) =
    (response.request.method == Request.GET || response.request.method == Request.HEAD) && response.body.isBuffered
}
