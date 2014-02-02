package uk.co.bigbeeconsultants.http.cache

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{Response, Status}
import uk.co.bigbeeconsultants.http.header._

class CacheStoreTest extends FunSuite {

  test("storing 100 responses up to the limit and then 100 more responses over the limit") {
    val store = new CacheStore(1000)
    assert(store.size === 0)
    val limit = 100

    // first phase - filling up an initially-empty cache store
    for (i <- 1 to limit) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (100000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(response)
      assert(store.size === i)
      assert(store.currentContentSize === i*10)
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    assert(store.size === limit)
    assert(store.currentContentSize === limit * 10)

    // second phase - overflowing and already-full cache store
    for (i <- limit + 1 to 2 * limit) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (200000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(response)
      assert(store.size === limit)
      assert(store.currentContentSize === limit * 10)
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    assert(store.size === limit)
    assert(store.currentContentSize === limit * 10)

    store.clear()
    assert(store.size === 0)
    assert(store.currentContentSize === 0L)
  }
}
