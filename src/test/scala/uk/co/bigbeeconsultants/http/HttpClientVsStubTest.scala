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
import request.Config
import scala.collection.JavaConversions._
import java.io._
import java.util.zip.GZIPOutputStream
import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.URL


class HttpClientVsStubTest extends FunSuite with BeforeAndAfter {

  import HttpClientTestUtils._

  val url = "/some/url"
  val config = Config(connectTimeout = 5000, readTimeout = 10000)

  private def convertHeaderList(headers: List[Header]): List[com.pyruby.stubserver.Header] = {
    headers.map {
      header => new com.pyruby.stubserver.Header (header.name, header.value)
    }
  }


  test ("get should return 200-OK") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.get (url)
    val json = """{"astring" : "the message" }"""
    server.expect (stubbedMethod).thenReturn (200, APPLICATION_JSON, json)

    val response = http.get (new URL (baseUrl + url))
    server.verify ()
    expect (APPLICATION_JSON)(response.body.contentType)
    expect (APPLICATION_JSON.toString)(response.headers.get (CONTENT_TYPE).value)
    expect (json)(response.body.toString)
  }


  test ("get should return 304-redirect") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.get (url)
    server.expect (stubbedMethod).thenReturn (304, APPLICATION_JSON, "ignore me")

    val response = http.get (new URL (baseUrl + url))
    server.verify ()
    expect (APPLICATION_JSON)(response.body.contentType)
    expect (APPLICATION_JSON.toString)(response.headers.get (CONTENT_TYPE).value)
    expect ("")(response.body.asString)
  }


  test ("get should set cookie and then send cookie") {
    val http = new HttpClient (config)
    val stubbedMethod1 = StubMethod.get (url)
    val json = """{"astring" : "the message" }"""
    val cookieHeaders = List (SET_COOKIE -> "foo=bar", SET_COOKIE -> ("dead=; Expires=" + HttpDateTimeInstant.zero))
    server.expect (stubbedMethod1).thenReturn (200, APPLICATION_JSON, json, convertHeaderList (cookieHeaders))

    val response1 = http.get (new URL (baseUrl + url))
    server.verify ()
    val jar1 = CookieJar.harvestCookies (response1)
    expect (1)(jar1.cookies.size)
    expect (1)(jar1.deleted.size)

    val stubbedMethod2 = stubbedMethod1.ifHeader (COOKIE.name, "foo=bar")
    server.expect (stubbedMethod2).thenReturn (200, APPLICATION_JSON, json)
    val response2 = http.get (new URL (baseUrl + url), Nil, jar1)
    server.verify ()
    val jar2 = CookieJar.harvestCookies (response2)
    expect (0)(jar2.cookies.size)
    expect (0)(jar2.deleted.size)
  }


  test ("get with gzip should return text") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.get (url)
    server.expect (stubbedMethod).thenReturn (200, TEXT_PLAIN.toString, toGzip (loadsOfText),
      convertHeaderList (List (CONTENT_ENCODING -> "gzip")))

    val response = http.get (new URL (baseUrl + url), Headers (List (ACCEPT_ENCODING -> "gzip")))
    server.verify ()
    val body = response.body
    expect (TEXT_PLAIN)(body.contentType)
    expect (loadsOfText)(body.toString)
    val accEnc = stubbedMethod.requestHeaders.get ("Accept-Encoding")
    expect ("gzip")(accEnc)
  }


  test ("head should return 200-OK") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.head (url)
    server.expect (stubbedMethod).thenReturn (200, TEXT_HTML, "")

    val response = http.head (new URL (baseUrl + url))
    server.verify ()
    expect (TEXT_HTML)(response.body.contentType)
    expect ("")(response.body.asString)
  }


  test ("get several urls should close OK") {
    val http = new HttpClient (config)
    val json = """{"a" : "b" }"""
    val n = 100
    for (i <- 1 to n) {
      val stubbedMethod = StubMethod.get (url + i)
      server.expect (stubbedMethod).thenReturn (200, APPLICATION_JSON, json)
    }

    val before = System.currentTimeMillis ()
    for (i <- 1 to n) {
      val response = http.get (new URL (baseUrl + url + i))
      expect (APPLICATION_JSON)(response.body.contentType)
      expect (json)(response.body.toString)
    }
    val after = System.currentTimeMillis ()
    server.verify ()
    println ((after - before) + "ms")
    http.closeConnections ()
  }


  test ("put should return 200-OK") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.put (url)
    val jsonReq = """{"astring" : "the request" }"""
    val jsonRes = """{"astring" : "the response" }"""
    server.expect (stubbedMethod).thenReturn (200, APPLICATION_JSON.toString, jsonRes)

    val response = http.put (new URL (baseUrl + url), request.RequestBody (APPLICATION_JSON, jsonReq))
    server.verify ()
    expect (APPLICATION_JSON)(response.body.contentType)
    expect (jsonRes)(response.body.toString)
  }


  test ("post should return 200-OK") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.post (url)
    val jsonRes = """{"astring" : "the response" }"""
    server.expect (stubbedMethod).thenReturn (200, APPLICATION_JSON, jsonRes)

    val response = http.post (new URL (baseUrl + url), request.RequestBody (APPLICATION_JSON, Map ("a" -> "b")))
    server.verify ()
    expect (APPLICATION_JSON)(response.body.contentType)
    expect (jsonRes)(response.body.toString)
  }


  test ("post should return 204-OK") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.post (url)
    server.expect (stubbedMethod).thenReturn (204, APPLICATION_JSON, "ignore me")

    val response = http.post (new URL (baseUrl + url), request.RequestBody (APPLICATION_JSON, Map ("a" -> "b")))
    server.verify ()
    expect (APPLICATION_JSON)(response.body.contentType)
    expect ("")(response.body.asString)
  }


  // chunked data not yet implemented and HttpURLConnection may be too buggy anyway
  ignore ("post with chunk size should set chunk header") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.post (url)
    server.expect (stubbedMethod).thenReturn (200, APPLICATION_JSON, "")
    http.post (new URL (baseUrl + url), request.RequestBody (APPLICATION_JSON, Map ("a" -> "b")))
    server.verify ()
    val transferEncoding = stubbedMethod.requestHeaders.get (TRANSFER_ENCODING)
    expect ("")(transferEncoding)
  }


  test ("delete should return 204-OK") {
    val http = new HttpClient (config)
    val stubbedMethod = StubMethod.delete (url)
    server.expect (stubbedMethod).thenReturn (204, APPLICATION_JSON, "")

    val response = http.delete (new URL (baseUrl + url))
    server.verify ()
    expect (APPLICATION_JSON)(response.body.contentType)
    expect ("")(response.body.asString)
  }


  test ("confirm empty buffer is immutable") {
    val emptyBuffer = ByteBuffer.allocateDirect (0)
    expect (0)(emptyBuffer.capacity ())
    try {
      emptyBuffer.get ()
      fail ()
    }
    catch {
      case be: BufferUnderflowException =>
    }
    try {
      emptyBuffer.putInt (0);
      fail ()
    }
    catch {
      case be: BufferOverflowException =>
    }
  }


  test ("soak test jpg image 200-OK") {
    val http = new HttpClient (config)
    val is = getClass.getClassLoader.getResourceAsStream ("plataria-sunset.jpg")
    val jpgBytes = Util.copyToByteBufferAndClose (is).array ()
    val size = jpgBytes.length
    val loops = 700
    val before = System.currentTimeMillis ()
    for (i <- 1 to loops) {
      val stubbedMethod = StubMethod.get (url)
      server.expect (stubbedMethod).thenReturn (200, IMAGE_JPG, jpgBytes)
      val response = http.get (new URL (baseUrl + url))
      expect (200)(response.status.code)
      expect (IMAGE_JPG)(response.body.contentType)
      val bytes = response.body.asBytes
      expect (size)(bytes.length)
      server.verify ()
      server.clearExpectations ()
    }
    val duration = System.currentTimeMillis () - before
    val bytes = BigDecimal (size * loops)
    val rate = (bytes / duration)
    println (bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
  }

  before {
    server = new StubServer (port)
    server.start ()
  }

  after {
    server.clearExpectations ()
    HttpClient.closeConnections ()
    server.stop ()

    //    if (CleanupThread.isRunning) {
    //      HttpClient.terminate()
    //      expect(false, "Expect that cleanup thread has been successully shut down") (CleanupThread.isRunning)
    //    }
  }

  private val port = (java.lang.Math.random * 16000 + 10000).asInstanceOf[Int]
  private val baseUrl = "http://localhost:" + port
  private var server: StubServer = null
}

object HttpClientTestUtils {

  lazy val loadsOfText: String = {
    var is = getClass.getClassLoader.getResourceAsStream ("test-lighthttpclient.php")
    if (is == null) {
      is = new FileInputStream("./src/test/resources/test-lighthttpclient.php")
    }
    assert (is != null)
    try {
      val br = new BufferedReader (new InputStreamReader (new BufferedInputStream (is), "UTF-8"))
      val sb = new StringBuilder
      var line = br.readLine ()
      while (line != null) {
        sb.append (line)
        line = br.readLine ()
      }
      sb.toString ()
    } finally {
      is.close ()
    }
  }

  def toGzip(s: String): Array[Byte] = {
    val is = new ByteArrayInputStream (s.getBytes ("UTF-8"))
    val baos = new ByteArrayOutputStream
    val gs = new GZIPOutputStream (baos)
    Util.copyBytes (is, gs)
    gs.close ()
    baos.toByteArray
  }
}
