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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.pyruby.stubserver.{StubServer, StubMethod}
import com.pyruby.stubserver.{Header => StubHeader}
import java.net.URL
import java.util
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.{HttpBrowser, Config, HttpClientTestUtils}
import uk.co.bigbeeconsultants.http.header.HttpDateTimeInstant
import scala.collection.JavaConversions

@RunWith(classOf[JUnitRunner])
class CacheVsStubTest extends FunSuite with BeforeAndAfter {

  import HttpClientTestUtils._

  val path = "/some/url"
  val config = Config(connectTimeout = 15000, readTimeout = 20000)

  //  private def convertHeaderList(headers: List[Header]): List[com.pyruby.stubserver.Header] = {
  //    headers.map {
  //      header => com.pyruby.stubserver.Header.header(header.name, header.value)
  //    }
  //  }

  test("correct caching and revalidation for a conditional get") {
    val http = new HttpBrowser(config)
    val date1 = new HttpDateTimeInstant
    val lastMod = date1 - 240
    val expires = date1 + 3600
    val stubbedMethod1 = StubMethod.get(path)
    server.expect(stubbedMethod1).thenReturn(200, TEXT_PLAIN, loadsOfText,
      toStubHeaders(
        "Date" -> date1,
        "Last-Modified" -> lastMod,
        "Expires" -> expires,
        "Cache-Control" -> "max-age=3600,must-revalidate"))

    // first request - misses the cache
    val url = new URL(baseUrl + path)
    val response1 = http.get(url)
    server.verify()
    assert(response1.status.code === 200)
    assert(response1.body.contentType === TEXT_PLAIN)
    assert(response1.body.asString === loadsOfText)
    assert(response1.headers(DATE).toDate.get === date1)
    assert(response1.headers(CACHE_CONTROL).value === "max-age=3600,must-revalidate")
    server.clearExpectations()

    // wait a while...
    // (darn it! nasty temporal test needs rewriting!)
    // FIXME - this will cause statistical test failures
    Thread.sleep(1000 - (System.currentTimeMillis() - date1.milliseconds))
    val date2 = new HttpDateTimeInstant
//    val stubbedMethod2 = StubMethod.get(path)
//    server.expect(stubbedMethod2).thenReturn(200, TEXT_PLAIN, loadsOfText,
//      toStubHeaders(
//        "Date" -> date2,
//        "Last-Modified" -> lastMod,
//        "Expires" -> expires,
//        "Cache-Control" -> "max-age=3600,must-revalidate"))

    // second request
    val response2 = http.get(url)
    server.verify()
    assert(response1.status.code === 200)
    assert(response2.body.contentType === TEXT_PLAIN)
    assert(response2.body.asString === loadsOfText)
    assert(response2.headers(DATE).toDate.get === date2)
    assert(response2.headers(AGE).value === "1") // confirms that the response was cached
    server.clearExpectations()

    // simulate waiting one hour...

    val response3 = http.get(url)
    server.verify()
    assert(response3.body.contentType === TEXT_PLAIN)
    assert(response3.body.asString === loadsOfText)
    assert(response3.headers(DATE).toDate.get === date2)
    // assert response is from origin server
    // assert status == 200
  }

  private def toStubHeaders(headers: (String, AnyRef)*): util.List[StubHeader] = {
    val stubs = for (hdr <- headers) yield {
      StubHeader.header(hdr._1, hdr._2.toString)
    }
    JavaConversions.seqAsJavaList(stubs.toList)
  }

  before {
    server = new StubServer()
    server.start()
    port = server.getLocalPort
    baseUrl = "http://localhost:" + port
  }

  after {
    server.clearExpectations()
    server.stop()
  }

  private var port = 0
  private var baseUrl: String = _
  private var server: StubServer = _
}
