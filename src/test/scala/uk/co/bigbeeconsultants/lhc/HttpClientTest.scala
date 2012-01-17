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

package uk.co.bigbeeconsultants.lhc

import com.pyruby.stubserver.StubMethod
import com.pyruby.stubserver.StubServer
import org.junit.{Test, AfterClass, BeforeClass}
import org.junit.Assert._
import java.net.{CookieHandler, URL}


class HttpClientTest {

  import HttpClientTest._

  @Test def get_shouldReturnOK() {
    val http = new HttpClient()
    val url = "/some/url"
    val stubbedMethod = StubMethod.get(url)
    val json = """{"astring" : "the message" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, json)
    val response = http.get(new URL(baseUrl + url))
    assertEquals(MediaType.APPLICATION_JSON, response.contentType)
    assertEquals(json, response.body)
    val accEnc = stubbedMethod.headers.get("Accept-Encoding")
//    assertEquals("gzip", accEnc)
  }

  @Test def get_severalUrls_shouldCloseOK() {
    val http = new HttpClient(false)
    val url = "/some/url"
    val json = """{"a" : "b" }"""
    val n = 1000
    for (i <- 1 to n) {
      val stubbedMethod = StubMethod.get(url + i)
      server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, json)
    }

    val before = System.currentTimeMillis()
    for (i <- 1 to n) {
      val response = http.get(new URL(baseUrl + url + i))
      assertEquals(MediaType.APPLICATION_JSON, response.contentType)
      assertEquals(json, response.body)
    }
    val after = System.currentTimeMillis()
    println((after - before) + "ms")
    http.closeConnections()
  }

  @Test def put_shouldReturnOK() {
    val http = new HttpClient()
    val url = "/some/url"
    val stubbedMethod = StubMethod.put(url)
    val jsonReq = """{"astring" : "the request" }"""
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, jsonRes)
    val response = http.put(new URL(baseUrl + url), Body(MediaType.APPLICATION_JSON, jsonReq))
    assertEquals(MediaType.APPLICATION_JSON, response.contentType)
    assertEquals(jsonReq, stubbedMethod.body)
    assertEquals(jsonRes, response.body)
  }

  @Test def post_shouldReturnOK() {
    val http = new HttpClient()
    val url = "/some/url"
    val stubbedMethod = StubMethod.post(url)
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, jsonRes)
    val response = http.post(new URL(baseUrl + url), Body(MediaType.APPLICATION_JSON, List(KeyVal("a", "b"))))
    assertEquals(MediaType.APPLICATION_JSON, response.contentType)
    assertEquals(jsonRes, response.body)
  }

  @Test def delete_shouldReturnOK() {
    val http = new HttpClient()
    val url = "/some/url"
    val stubbedMethod = StubMethod.delete(url)
    server.expect(stubbedMethod).thenReturn(204, MediaType.APPLICATION_JSON.toString, "")
    val response = http.delete(new URL(baseUrl + url))
    assertEquals(MediaType.APPLICATION_JSON, response.contentType)
    assertEquals("", response.body)
  }
}

object HttpClientTest {
  private val port = (java.lang.Math.random * 16000 + 10000).asInstanceOf[Int]
  private var baseUrl: String = null
  private var server: StubServer = null

  @BeforeClass def configure() {
    val ch = CookieHandler.getDefault()
    baseUrl = "http://localhost:" + port
    server = new StubServer(port)
    server.start()
  }

  @AfterClass def after() {
    server.stop()
  }
}