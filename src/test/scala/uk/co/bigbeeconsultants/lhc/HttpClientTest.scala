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
import header.{HeaderName, Headers, MediaType}
import org.junit.Assert._
import java.nio.{BufferUnderflowException, BufferOverflowException, ByteBuffer}
import java.net.URL
import request.Body
import response.CachedBody
import org.junit._
import HeaderName._
import MediaType._
import scala.collection.JavaConversions._
import java.io._
import java.util.zip.GZIPOutputStream


class HttpClientTest {

  import HttpClientTest._

  val url = "/some/url"

  @Test def get200_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.get(url)
    val json = """{"astring" : "the message" }"""
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, json)

    val response = http.get(new URL(baseUrl + url))
    server.verify()
    assertEquals(APPLICATION_JSON, response.body.contentType)
    assertEquals(APPLICATION_JSON.toString, response.headers.get(CONTENT_TYPE).value)
    assertEquals(json, response.body.asString)
  }


  @Test def get304_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.get(url)
    server.expect(stubbedMethod).thenReturn(304, APPLICATION_JSON, "ignore me")

    val response = http.get(new URL(baseUrl + url))
    server.verify()
    assertEquals(APPLICATION_JSON, response.body.contentType)
    assertEquals(APPLICATION_JSON.toString, response.headers.get(CONTENT_TYPE).value)
    assertEquals("", response.body.asString)
  }


  @Test def get_withGzip_shouldReturnText() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.get(url)
    server.expect(stubbedMethod).thenReturn(200, TEXT_PLAIN.toString, toGzip(HttpClientTest.loadsOfText), Map(CONTENT_ENCODING.name -> "gzip"))

    val response = http.get(new URL(baseUrl + url), Headers(List(ACCEPT_ENCODING -> "gzip")))
    server.verify()
    val body = response.body
    assertEquals(TEXT_PLAIN, body.contentType)
    assertEquals(HttpClientTest.loadsOfText, body.asString)
    val accEnc = stubbedMethod.headers.get("Accept-Encoding")
    assertEquals("gzip", accEnc)
  }


  @Test def head_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.head(url)
    server.expect(stubbedMethod).thenReturn(200, TEXT_HTML, "")

    val response = http.head(new URL(baseUrl + url))
    server.verify()
    assertEquals(TEXT_HTML, response.body.contentType)
    assertEquals("", response.body.asString)
  }


  @Test def get_severalUrls_shouldCloseOK() {
    val http = new HttpClient()
    val json = """{"a" : "b" }"""
    val n = 100
    for (i <- 1 to n) {
      val stubbedMethod = StubMethod.get(url + i)
      server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, json)
    }

    val before = System.currentTimeMillis()
    for (i <- 1 to n) {
      val response = http.get(new URL(baseUrl + url + i))
      assertEquals(APPLICATION_JSON, response.body.contentType)
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
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON.toString, jsonRes)

    val response = http.put(new URL(baseUrl + url), Body(APPLICATION_JSON, jsonReq))
    server.verify()
    assertEquals(APPLICATION_JSON, response.body.contentType)
    assertEquals(jsonRes, response.body.asString)
  }


  @Test def post200_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.post(url)
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, jsonRes)

    val response = http.post(new URL(baseUrl + url), Body(APPLICATION_JSON, Map("a" -> "b")))
    server.verify()
    assertEquals(APPLICATION_JSON, response.body.contentType)
    assertEquals(jsonRes, response.body.asString)
  }


  @Test def post204_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.post(url)
    server.expect(stubbedMethod).thenReturn(204, APPLICATION_JSON, "ignore me")

    val response = http.post(new URL(baseUrl + url), Body(APPLICATION_JSON, Map("a" -> "b")))
    server.verify()
    assertEquals(APPLICATION_JSON, response.body.contentType)
    assertEquals("", response.body.asString)
  }


  @Ignore // chnked data not yet implemented and HttpURLConnection may be too buggy anyway
  @Test def post_withChunkSize_shouldSetChunkHeader() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.post(url)
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, "")
    http.post(new URL(baseUrl + url), Body(APPLICATION_JSON, Map("a" -> "b")))
    server.verify()
    val transferEncoding = stubbedMethod.headers.get(TRANSFER_ENCODING)
    assertEquals("", transferEncoding)
  }


  @Test def delete_shouldReturnOK() {
    val http = new HttpClient()
    val stubbedMethod = StubMethod.delete(url)
    server.expect(stubbedMethod).thenReturn(204, APPLICATION_JSON, "")

    val response = http.delete(new URL(baseUrl + url))
    server.verify()
    assertEquals(APPLICATION_JSON, response.body.contentType)
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
  @Ignore
  def soakTestJpgOK() {
    val http = new HttpClient()
    val is = getClass.getClassLoader.getResourceAsStream("plataria-sunset.jpg")
    val jpgBytes = Util.copyToByteBufferAndClose(is).array()
    val size = jpgBytes.length
    val loops = 700
    val before = System.currentTimeMillis()
    for (i <- 1 to loops) {
      val stubbedMethod = StubMethod.get(url)
      server.expect(stubbedMethod).thenReturn(200, IMAGE_JPG, jpgBytes)
      val response = http.get(new URL(baseUrl + url))
      assertEquals(200, response.status.code)
      assertEquals(IMAGE_JPG, response.body.contentType)
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

  @Before
  def before() {
    server = new StubServer(port)
    server.start()
  }

  @After
  def after() {
    server.clearExpectations()
    HttpClient.closeConnections()
    server.stop()
  }
}

object HttpClientTest {
  private val port = (java.lang.Math.random * 16000 + 10000).asInstanceOf[Int]
  private var baseUrl: String = null
  private var server: StubServer = null

  @BeforeClass
  def configure() {
    baseUrl = "http://localhost:" + port
    //    server = new StubServer(port)
    //    server.start()
  }

  @AfterClass
  def finish() {
    if (CleanupThread.isRunning) {
      HttpClient.terminate()
      assertFalse("Expect that cleanup thread has been successully shut down", CleanupThread.isRunning)
    }
    //    server.stop()
  }


  lazy val loadsOfText: String = {
    val is = getClass.getClassLoader.getResourceAsStream("test-lighthttpclient.php")
    try {
      val br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "UTF-8"))
      val sb = new StringBuilder
      var line = br.readLine()
      while (line != null) {
        sb.append(line)
        line = br.readLine()
      }
      sb.toString()
    } finally {
      is.close()
    }
  }

  def toGzip(s: String): Array[Byte] = {
    val is = new ByteArrayInputStream(s.getBytes("UTF-8"))
    val baos = new ByteArrayOutputStream
    val gs = new GZIPOutputStream(baos)
    Util.copyBytes(is, gs)
    gs.close()
    baos.toByteArray
  }
}