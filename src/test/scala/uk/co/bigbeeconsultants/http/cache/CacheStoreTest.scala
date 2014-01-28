package uk.co.bigbeeconsultants.http.cache

import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.{Response, Status}
import uk.co.bigbeeconsultants.http.header._

class CacheStoreTest extends FunSuite {

  test("1") {
    val store = new CacheStore(1000)
    assert(store.size === 0)

    // first phase - filling up an initially-empty cache store
    for (i <- 1 to 100) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (1000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(response)
      assert(store.size === i)
      assert(store.currentContentSize === i*10)
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    assert(store.size === 100)
    assert(store.currentContentSize === 1000)

    // second phase - overflowing and already-full cache store
    for (i <- 101 to 200) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (2000 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(response)
      assert(store.size === 100)
      assert(store.currentContentSize === 1000)
      assert(store.get(request.cacheKey) != null)
      assert(store.get(request.cacheKey).response === response)
    }

    assert(store.size === 100)
    assert(store.currentContentSize === 1000)

    store.clear()
    assert(store.size === 0)
    assert(store.currentContentSize === 0L)
  }
}
