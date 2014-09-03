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

import java.util.concurrent.{ArrayBlockingQueue, ConcurrentHashMap, LinkedTransferQueue}

private[http] class CacheStore(maxContentSize: Long, lazyCleanup: Boolean, queueLimit: Int = Int.MaxValue) {

  if (lazyCleanup) startBackgroundThread()

  // these fields are concurrently accessed by many threads
  private val data = new ConcurrentHashMap[CacheKey, CacheRecord]()

  // these fields are only accessed by one thread at a time
  private var sortedRecords = List[CacheRecord]()
  private[cache] var currentContentSize = 0L

  def size = data.size

  // lock-free lookups via ConcurrentHashMap
  def get(key: CacheKey) = data.get(key)

  // lock-based storing
  def put(record: CacheRecord) = {
    putRecord(record)
  }

  def putIfControlled(record: CacheRecord) = {
    if (record.expires.isDefined || record.cacheControlHeader.isDefined) {
      putRecord(record)
    }
  }

  private def putRecord(record: CacheRecord) {
    if (!record.isAlreadyExpired) {
      val previous = data.put(record.response.request.cacheKey, record)
      if (lazyCleanup)
        submitForCleaning(previous, record)
      else
        synchronized {
          gc(previous, record)
        }
    }
  }

  // this is not synchronised so only works when run inside a single thread
  private def gc(oldRecord: CacheRecord, newRecord: CacheRecord) {
    if (oldRecord != null) {
      val prevId = oldRecord.id
      sortedRecords = sortedRecords.filterNot(_.id == prevId)
      currentContentSize -= oldRecord.contentLength
    }
    sortedRecords = newRecord :: sortedRecords
    sortedRecords = sortedRecords.sorted
    currentContentSize += newRecord.contentLength

    while (currentContentSize > maxContentSize) {
      val doomed = sortedRecords.head
      data.remove(doomed.response.request.cacheKey)
      currentContentSize -= doomed.contentLength
      sortedRecords = sortedRecords.tail
    }
  }

  private def gc(item: Item) {
    gc(item.oldRecord, item.newRecord)
  }

  def clear() {
    synchronized {
      data.clear()
      sortedRecords = List[CacheRecord]()
      currentContentSize = 0L
      _cleanupCount = 0
    }
  }

  private lazy val queue =
    if (queueLimit < Int.MaxValue) new ArrayBlockingQueue[Item](queueLimit)
    else new LinkedTransferQueue[Item]()

  private def submitForCleaning(oldRecord: CacheRecord, newRecord: CacheRecord) {
    queue.put(new Item(oldRecord, newRecord))
  }

  // there is only one background thread, avoiding any need for synchronization on the mutable state
  private lazy val cleanerThread = {
    val runnable = new Runnable() {
      def run() {
        while (true) {
          try {
            gc(queue.take())
            _cleanupCount += 1
          } catch {
            case e: Exception => e.printStackTrace()
          }
        }
      }
    }
    val thread = new Thread(runnable, "CacheStore-cleaner")
    thread.setDaemon(true)
    thread.setPriority(Thread.MIN_PRIORITY)
    thread.start()
    thread
  }

  private def startBackgroundThread() {
    cleanerThread.toString // force lazy evaluation
  }

  def cleanupCount = _cleanupCount

  // track operation for test purposes
  private var _cleanupCount = 0
}

private case class Item(oldRecord: CacheRecord, newRecord: CacheRecord)
