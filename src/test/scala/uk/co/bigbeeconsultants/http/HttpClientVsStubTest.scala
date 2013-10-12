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

package uk.co.bigbeeconsultants.http

import com.pyruby.stubserver.StubMethod
import com.pyruby.stubserver.StubServer
import header._
import java.nio.{BufferUnderflowException, BufferOverflowException, ByteBuffer}
import HeaderName._
import MediaType._
import scala.collection.JavaConversions._
import java.io._
import java.util.zip._
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.URL
import util.HttpUtil._


class HttpClientVsStubTest extends FunSuite with BeforeAndAfter {

  import HttpClientTestUtils._

  val url = "/some/url"
  val config = Config(connectTimeout = 15000, readTimeout = 10000)

  private def convertHeaderList(headers: List[Header]): List[com.pyruby.stubserver.Header] = {
    headers.map {
      header => new com.pyruby.stubserver.Header(header.name, header.value)
    }
  }


  test("get should return 200-OK") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.get(url)
    val json = """{"url" : "http://somewhere.org/a/path" }"""
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, json)

    val response = http.get(new URL(baseUrl + url))
    server.verify()
    assert(APPLICATION_JSON === response.body.contentType)
    assert(APPLICATION_JSON.toString === response.headers(CONTENT_TYPE).value)
    assert(json === response.body.asString)
  }


  test("get should return 304-redirect") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.get(url)
    server.expect(stubbedMethod).thenReturn(304, APPLICATION_JSON, "ignore me")

    val response = http.get(new URL(baseUrl + url))
    server.verify()
    assert(APPLICATION_JSON === response.body.contentType)
    assert(APPLICATION_JSON.toString === response.headers(CONTENT_TYPE).value)
    assert("" === response.body.asString)
  }


  test("get should set cookie and then send cookie") {
    val http = new HttpClient(config)
    val stubbedMethod1 = StubMethod.get(url)
    val json = """{"astring" : "the message" }"""
    val cookieHeaders = List(SET_COOKIE -> "foo=bar", SET_COOKIE -> ("dead=; Expires=" + HttpDateTimeInstant.zero))
    server.expect(stubbedMethod1).thenReturn(200, APPLICATION_JSON, json, convertHeaderList(cookieHeaders))

    val response1 = http.get(new URL(baseUrl + url), Nil, CookieJar.Empty)
    server.verify()
    val jar1 = response1.cookies.get
    assert(1 === jar1.size)

    val stubbedMethod2 = stubbedMethod1.ifHeader(COOKIE.name, "foo=bar")
    server.expect(stubbedMethod2).thenReturn(200, APPLICATION_JSON, json)
    val response2 = http.get(new URL(baseUrl + url), Nil, jar1)
    server.verify()
    val jar2 = response2.cookies.get
    assert(jar1 === jar2)
  }


  test("get with gzip should return text") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.get(url)
    server.expect(stubbedMethod).thenReturn(200, TEXT_PLAIN.toString, gzip(loadsOfText),
      convertHeaderList(List(CONTENT_ENCODING -> "gzip")))

    val response = http.get(new URL(baseUrl + url), Headers(List(ACCEPT_ENCODING -> "gzip")))
    server.verify()
    val body = response.body
    assert(TEXT_PLAIN === body.contentType)
    assert(loadsOfText === body.asString)
    val accEnc = stubbedMethod.requestHeaders.get("Accept-Encoding")
    assert("gzip" === accEnc)
  }


  // not working
//  test("get with deflate should return text") {
//    val http = new HttpClient(config)
//    val stubbedMethod = StubMethod.get(url)
//    server.expect(stubbedMethod).thenReturn(200, TEXT_PLAIN.toString, deflate(loadsOfText),
//      convertHeaderList(List(CONTENT_ENCODING -> "deflate")))
//
//    val response = http.get(new URL(baseUrl + url), Headers(List(ACCEPT_ENCODING -> "deflate")))
//    server.verify()
//    val body = response.body
//    assert(TEXT_PLAIN === body.contentType)
//    assert(loadsOfText === body.toString)
//    val accEnc = stubbedMethod.requestHeaders.get("Accept-Encoding")
//    assert("deflate" === accEnc)
//  }


  test("head should return 200-OK") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.head(url)
    server.expect(stubbedMethod).thenReturn(200, TEXT_HTML, "")

    val response = http.head(new URL(baseUrl + url))
    server.verify()
    assert(TEXT_HTML === response.body.contentType)
    assert("" === response.body.asString)
  }


  test("get several urls should close OK") {
    val http = new HttpClient(config)
    val json = """{"a" : "b" }"""
    val n = 100
    for (i <- 1 to n) {
      val stubbedMethod = StubMethod.get(url + i)
      server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, json)
    }

    val before = System.currentTimeMillis()
    for (i <- 1 to n) {
      val response = http.get(new URL(baseUrl + url + i))
      assert(APPLICATION_JSON === response.body.contentType)
      assert(json === response.body.asString)
    }
    val after = System.currentTimeMillis()
    server.verify()
    println((after - before) + "ms")
  }


  test("put should return 200-OK") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.put(url)
    val jsonReq = """{"astring" : "the request" }"""
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON.toString, jsonRes)

    val response = http.put(new URL(baseUrl + url), request.RequestBody(jsonReq, APPLICATION_JSON))
    server.verify()
    assert(APPLICATION_JSON === response.body.contentType)
    assert(jsonRes === response.body.asString)
  }


  test("post should return 200-OK") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.post(url)
    val jsonRes = """{"astring" : "the response" }"""
    server.expect(stubbedMethod).thenReturn(200, APPLICATION_JSON, jsonRes)

    val response = http.post(new URL(baseUrl + url), Some(request.RequestBody(Map("a" -> "b"), APPLICATION_JSON)))
    server.verify()
    assert(APPLICATION_JSON === response.body.contentType)
    assert(jsonRes === response.body.asString)
  }


  test("post should return 204-NoContent with text response") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.post(url)
    server.expect(stubbedMethod).thenReturn(204, APPLICATION_JSON, "ignore me")

    val response = http.post(new URL(baseUrl + url), Some(request.RequestBody(Map("a" -> "b"), APPLICATION_JSON)))
    server.verify()
    assert(APPLICATION_JSON === response.body.contentType)
    assert("" === response.body.asString)
  }


  test("put should return 204-NoContent with binary response") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.put(url)
    server.expect(stubbedMethod).thenReturn(204, APPLICATION_OCTET_STREAM, "")

    val response = http.put(new URL(baseUrl + url), request.RequestBody(Map("a" -> "b"), APPLICATION_JSON))
    server.verify()
    assert(APPLICATION_OCTET_STREAM === response.body.contentType)
    assert(false === response.body.isTextual)
  }


  test("delete should return 204-OK") {
    val http = new HttpClient(config)
    val stubbedMethod = StubMethod.delete(url)
    server.expect(stubbedMethod).thenReturn(204, APPLICATION_JSON, "")

    val response = http.delete(new URL(baseUrl + url))
    server.verify()
    assert(APPLICATION_JSON === response.body.contentType)
    assert("" === response.body.asString)
  }


  test("confirm empty buffer is immutable") {
    val emptyBuffer = ByteBuffer.allocateDirect(0)
    assert(0 === emptyBuffer.capacity())
    try {
      emptyBuffer.get()
      fail()
    }
    catch {
      case be: BufferUnderflowException =>
    }
    try {
      emptyBuffer.putInt(0)
      fail()
    }
    catch {
      case be: BufferOverflowException =>
    }
  }


  test("soak test jpg image 200-OK") {
    val http = new HttpClient(config)
    val is = getClass.getClassLoader.getResourceAsStream("plataria-sunset.jpg")
    val jpgBytes = copyToByteArrayAndClose(is)
    val size = jpgBytes.length
    val loops = 100
    val before = System.currentTimeMillis()
    for (i <- 1 to loops) {
      val stubbedMethod = StubMethod.get(url)
      server.expect(stubbedMethod).thenReturn(200, IMAGE_JPG, jpgBytes)
      val response = http.get(new URL(baseUrl + url))
      assert(200 === response.status.code)
      assert(IMAGE_JPG === response.body.contentType)
      val bytes = response.body.asBytes
      assert(size === bytes.length)
      server.verify()
      server.clearExpectations()
    }
    val duration = System.currentTimeMillis() - before
    val bytes = BigDecimal(size * loops)
    val rate = bytes / duration
    println(bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
  }

  before {
    server = new StubServer(port)
    server.start()
  }

  after {
    server.clearExpectations()
    server.stop()

    //    if (CleanupThread.isRunning) {
    //      HttpClient.terminate()
    //      assert(false, "Expect that cleanup thread has been successully shut down") (CleanupThread.isRunning)
    //    }
  }

  private val port = (java.lang.Math.random * 16000 + 10000).asInstanceOf[Int]
  private val baseUrl = "http://localhost:" + port
  private var server: StubServer = null
}

object HttpClientTestUtils {

  lazy val loadsOfText: String = {
    var is = getClass.getClassLoader.getResourceAsStream("test-lighthttpclient.php")
    if (is == null) {
      is = new FileInputStream("./src/test/resources/test-lighthttpclient.php")
    }
    assert(is != null)
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

  def gzip(s: String): Array[Byte] = {
    val is = new ByteArrayInputStream(s.getBytes("UTF-8"))
    val baos = new ByteArrayOutputStream
    val gos = new GZIPOutputStream(baos)
    copyBytes(is, gos)
    gos.close()
    baos.toByteArray
  }

//  def deflate(s: String): Array[Byte] = {
//    val is = s.getBytes("UTF-8")
//    val compressor = new Deflater()
//    compressor.setInput(is)
//    compressor.finish()
//    val output = new Array[Byte](100)
//    val compressedDataLength = compressor.deflate(output)
//    val compressed = new Array[Byte](compressedDataLength)
//    Array.copy(output, 0, compressed, 0, compressedDataLength)
//    compressed
//  }
}
