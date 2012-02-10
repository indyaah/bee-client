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
import header.{Header, MediaType}
import org.junit.Assert._
import java.nio.{BufferUnderflowException, BufferOverflowException, ByteBuffer}
import org.junit.{After, Test, AfterClass, BeforeClass}
import java.net.URL
import request.Body
import response.CachedBody


class HttpClientTest {

  import HttpClientTest._

  val url = "/some/url"

  @Test def get_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.get(url)
    val json = """{"astring" : "the message" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, json)

    val response = http.get(new URL(baseUrl + url))
    server.verify()
    assertEquals(MediaType.APPLICATION_JSON, response.body.contentType)
    assertEquals(MediaType.APPLICATION_JSON.toString, response.headers("CONTENT-TYPE").value)
    assertEquals(json, response.body.asString)
  }

  @Test def get_withRequestParams_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.get(url)
    val json = """{"astring" : "the message" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, json)

    val response = http.get(new URL(baseUrl + url), List(Header("Accept-Encoding", "gzip")))
    server.verify()
    assertEquals(MediaType.APPLICATION_JSON, response.body.contentType)
    assertEquals(json, response.body.asString)
    val accEnc = stubbedMethod.headers.get("Accept-Encoding")
    assertEquals("gzip", accEnc)
  }

  @Test def head_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.head(url)
    server.expect(stubbedMethod).thenReturn(200, MediaType.TEXT_HTML.toString, "")

    val response = http.head(new URL(baseUrl + url))
    server.verify()
    assertEquals(MediaType.TEXT_HTML, response.body.contentType)
    assertEquals("", response.body.asString)
  }

  @Test def get_severalUrls_shouldCloseOK() {
    val http = new HttpClient(false)
    val json = """{"a" : "b" }"""
    val n = 100
    for (i <- 1 to n) {
      val stubbedMethod = StubMethod.get(url + i)
      server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, json)
    }

    val before = System.currentTimeMillis()
    for (i <- 1 to n) {
      val response = http.get(new URL(baseUrl + url + i))
      assertEquals(MediaType.APPLICATION_JSON, response.body.contentType)
      assertEquals(json, response.body.asString)
    }
    val after = System.currentTimeMillis()
    server.verify()
    println((after - before) + "ms")
    http.closeConnections()
  }

  @Test def put_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.put(url)
    val jsonReq = """{"astring" : "the request" }"""
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, jsonRes)

    val response = http.put(new URL(baseUrl + url), Body(MediaType.APPLICATION_JSON, jsonReq))
    server.verify()
    assertEquals(MediaType.APPLICATION_JSON, response.body.contentType)
    assertEquals(jsonRes, response.body.asString)
  }

  @Test def post_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.post(url)
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, MediaType.APPLICATION_JSON.toString, jsonRes)

    val response = http.post(new URL(baseUrl + url), Body(MediaType.APPLICATION_JSON, Map("a" -> "b")))
    server.verify()
    assertEquals(MediaType.APPLICATION_JSON, response.body.contentType)
    assertEquals(jsonRes, response.body.asString)
  }

  @Test def delete_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.delete(url)
    server.expect(stubbedMethod).thenReturn(204, MediaType.APPLICATION_JSON.toString, "")

    val response = http.delete(new URL(baseUrl + url))
    server.verify()
    assertEquals(MediaType.APPLICATION_JSON, response.body.contentType)
    assertEquals("", response.body.asString)
  }

  @Test
  def confirmEmptyBufferIsImmutable() {
    val emptyBuffer = ByteBuffer.allocateDirect(0)
    assertEquals(0, emptyBuffer.capacity())
    try {
      emptyBuffer.get()
      fail()
    }
    catch {
      case be: BufferUnderflowException =>
    }
    try {
      emptyBuffer.putInt(0);
      fail()
    }
    catch {
      case be: BufferOverflowException =>
    }
  }

  @Test
  def soakTestJpgOK() {
    val http = new HttpClient()
    val is = getClass.getClassLoader.getResourceAsStream("plataria-sunset.jpg")
    val jpgBytes = Util.copyToByteBufferAndClose(is).array()
    val size = jpgBytes.length
    val loops = 700
    val before = System.currentTimeMillis()
    for (i <- 1 to loops) {
      val stubbedMethod = StubMethod.get(url)
      server.expect(stubbedMethod).thenReturn(200, MediaType.IMAGE_JPG.toString, jpgBytes)
      val response = http.get(new URL(baseUrl + url))
      assertEquals(200, response.status.code)
      assertEquals(MediaType.IMAGE_JPG, response.body.contentType)
      val bytes = response.body.asInstanceOf[CachedBody].asBytes
      assertEquals(size, bytes.length)
      server.verify()
      server.clearExpectations()
    }
    val duration = System.currentTimeMillis() - before
    val bytes = BigDecimal(size * loops)
    val rate = (bytes / duration)
    println(bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
  }

  @After
  def after() {
    server.clearExpectations()
  }
}

object HttpClientTest {
  private val port = (java.lang.Math.random * 16000 + 10000).asInstanceOf[Int]
  private var baseUrl: String = null
  private var server: StubServer = null

  @BeforeClass def configure() {
    baseUrl = "http://localhost:" + port
    server = new StubServer(port)
    server.start()
  }

  @AfterClass def after() {
    server.stop()
  }
}