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
    for (i <- 1 to 100) {
      val request = Request.get("http://localhost/stuff" + i)
      val f = (i % 2) * -1
      val age: Header = HeaderName.AGE -> (100 - f * i).toString
      val response = Response(request, Status.S200_OK, MediaType.TEXT_PLAIN, "0123456789", Headers(age))
      store.put(response)
      assert(store.size === i)
    }
  }
}
