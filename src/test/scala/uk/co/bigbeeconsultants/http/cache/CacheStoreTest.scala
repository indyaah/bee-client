package uk.co.bigbeeconsultants.http.cache

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{Response, Status}
import uk.co.bigbeeconsultants.http.header._
import uk.co.bigbeeconsultants.http.util.DiagnosticTimer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CacheStoreTest extends FunSuite {

  test("storing 500 responses up to the limit and then 500 more responses over the limit (eager cleanup)") {
    val loops = 500
    val maxContentSize = loops * 10
    val store = new CacheStore(maxContentSize, false)
    assert(store.size === 0)
    val dt = new DiagnosticTimer

    // first phase - filling up an initially-empty cache store
    for (i <- 1 to loops) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (100000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(CacheRecord(response))
      assert(store.size === i)
      assert(store.currentContentSize === i*10)
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    assert(store.size === loops)
    assert(store.currentContentSize === loops * 10)

    // second phase - overflowing and already-full cache store
    for (i <- loops + 1 to 2 * loops) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (200000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(CacheRecord(response))
      assert(store.size === loops)
      assert(store.currentContentSize === loops * 10)
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    val d = dt.duration / loops

    assert(store.size === loops)
    assert(store.currentContentSize === loops * 10)
    assert(store.cleanupCount === 0)

    store.clear()
    assert(store.size === 0)
    assert(store.currentContentSize === 0L)

    println("put took " + d + " per loop (eager cleanup)")
  }


  test("storing 500 responses up to the limit and then 500 more responses over the limit - lazy cleanup with infinite queue") {
    val loops = 500
    val maxContentSize = loops * 10
    val store = new CacheStore(maxContentSize, true)
    assert(store.size === 0)
    val dt = new DiagnosticTimer

    // first phase - filling up an initially-empty cache store
    for (i <- 1 to loops) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (100000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(CacheRecord(response))
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
      assert(store.size === i)
    }

    assert(store.size === loops)
    assert(store.currentContentSize > 0)

    // second phase - overflowing and already-full cache store
    for (i <- loops + 1 to 2 * loops) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (200000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(CacheRecord(response))
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    val d = dt.duration / loops

    var waitingRetries = 10
    while (waitingRetries > 0) {
      waitingRetries -= 1
      Thread.sleep(100) // allow the background thread some more time to catch up
      if (store.size == loops) waitingRetries = 0
    }

    assert(store.size === loops)
    assert(store.currentContentSize > 0)
    assert(store.cleanupCount === 2 * loops)

    store.clear()
    assert(store.size === 0)
    assert(store.currentContentSize === 0L)

    println("put took " + d + " per loop (lazy cleanup)")
  }


  test("storing 500 responses up to the limit and then 500 more responses over the limit - lazy cleanup with limited queue") {
    val loops = 500
    val maxContentSize = loops * 10
    val store = new CacheStore(maxContentSize, true, 250)
    assert(store.size === 0)
    val dt = new DiagnosticTimer

    // first phase - filling up an initially-empty cache store
    for (i <- 1 to loops) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (100000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(CacheRecord(response))
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
      assert(store.size === i)
    }

    assert(store.size === loops)
    assert(store.currentContentSize > 0)

    // second phase - overflowing and already-full cache store
    for (i <- loops + 1 to 2 * loops) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (200000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(CacheRecord(response))
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    val d = dt.duration / loops

    var waitingRetries = 10
    while (waitingRetries > 0) {
      waitingRetries -= 1
      Thread.sleep(100) // allow the background thread some more time to catch up
      if (store.size == loops) waitingRetries = 0
    }

    assert(store.size === loops)
    assert(store.currentContentSize > 0)
    assert(store.cleanupCount === 2 * loops)

    store.clear()
    assert(store.size === 0)
    assert(store.currentContentSize === 0L)

    println("put took " + d + " per loop (lazy cleanup)")
  }
}
