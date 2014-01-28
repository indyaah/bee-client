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
import uk.co.bigbeeconsultants.http.request.Request
import scala.collection.mutable

@deprecated("This is not yet ready for production use", "v0.25.1")
class Cache(maxContentSize: Int = Int.MaxValue) {

  private val data = new CacheStore(maxContentSize)

  private[cache] def size = data.size

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
    if (isCacheable(response)) {
      response.status.code match {
        case 200 | 203 | 300 | 301 | 410 => offerToCache(response)
        //case 404 => offerToCache(response)
        case _ => // cannot store this response
      }
    }
  }

  private def offerToCache(response: Response) {
    val cacheControl = response.headers.get(CACHE_CONTROL) map (_.toCacheControlValue)
    if (cacheControl.isDefined)
      cacheControl.get.label match {
        case "no-cache" | "no-store" => // cannot store this response
        case _ => writeToCache(response)
      } else writeToCache(response)
  }

  private def writeToCache(response: Response) {
    val record = CacheRecord(response)
    if (!record.isAlreadyExpired) {
      data.put(record)
    }
  }

  private def isCacheable(response: Response) =
    (response.request.method == Request.GET || response.request.method == Request.HEAD) && response.body.isBuffered
}


class CacheStore(maxContentSize: Int) {
  private val data = new ConcurrentHashMap[CacheKey, CacheRecord]()
  private var sortedRecords = mutable.ArrayStack[CacheRecord]()
  private var currentContentSize = 0L

  def size = data.size

  // lock-free lookups via ConcurrentHashMap
  def get(key: CacheKey) = data.get(key)

  // lock-based storing
  def put(record: CacheRecord) = {
    synchronized {
      val previous = data.put(record.response.request.cacheKey, record)
      if (previous != null) {
        sortedRecords.filterNot(_ == previous)
        currentContentSize -= previous.contentLength
      }
      sortedRecords = (sortedRecords :+ record).sorted
      currentContentSize += record.contentLength

      while (currentContentSize > maxContentSize) {
        val doomed = sortedRecords.pop()
        data.remove(doomed.response.request.cacheKey)
        currentContentSize -= doomed.contentLength
      }
    }
  }
}