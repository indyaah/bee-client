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
import scala.collection.mutable
import uk.co.bigbeeconsultants.http.response.Response
import java.util.concurrent.atomic.AtomicInteger

private[http] class CacheStore(maxContentSize: Int) {

  private val counter = new AtomicInteger()
  private val data = new ConcurrentHashMap[CacheKey, CacheRecord]()
  private var sortedRecords = List[CacheRecord]()
  private[cache] var currentContentSize = 0L

  def size = data.size

  // lock-free lookups via ConcurrentHashMap
  def get(key: CacheKey) = data.get(key)

  // lock-based storing
  def put(response: Response) = {
    val record = CacheRecord(response, counter.incrementAndGet())
    if (!record.isAlreadyExpired) {
      putRecord(record)
    }
  }

  private def putRecord(record: CacheRecord) {
    val previous = data.put(record.response.request.cacheKey, record)
    synchronized {
      if (previous != null) {
        val prevId = previous.id
        sortedRecords = sortedRecords.filterNot(_.id == prevId)
        currentContentSize -= previous.contentLength
      }
      sortedRecords = record :: sortedRecords
      sortedRecords = sortedRecords.sorted
      currentContentSize += record.contentLength

      while (currentContentSize > maxContentSize) {
        val doomed = sortedRecords.head
        data.remove(doomed.response.request.cacheKey)
        currentContentSize -= doomed.contentLength
        sortedRecords = sortedRecords.tail
      }
    }
  }

  def clear() {
    synchronized {
      data.clear()
      sortedRecords = List[CacheRecord]()
      currentContentSize = 0L
    }
  }
}
