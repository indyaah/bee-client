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

import header.{HeaderName, MediaType}
import java.net.{ConnectException, URL}
import java.lang.AssertionError
import request.{Config, Body}
import response.CachedBody
import org.junit.{After, Before, Test}
import org.junit.Assert._

class HttpIntegration {

  private val serverUrl = "http://localhost/lighthttpclient/"
  private val testScriptUrl = serverUrl + "test-lighthttpclient.php"
  private val testImageUrl = serverUrl + "B.png"
  private val testPhotoUrl = serverUrl + "plataria-sunset.jpg"
  private val jsonBody = Body(MediaType.APPLICATION_JSON, """{ "x": 1, "y": true }""")

  var http: HttpClient = _

  @Before
  def before() {
    http = new HttpClient(config = Config(followRedirects = false))
  }

  @After
  def close() {
    http.closeConnections()
  }

  @Test
  def setupOK() {
    //    assertFalse(http.config.keepAlive)
  }

  @Test
  def htmlHeadOK() {
    try {
      val response = http.head(new URL(testScriptUrl))
      assertEquals(200, response.status.code)
      val body = response.body
      assertEquals(MediaType.TEXT_HTML, body.contentType)
      assertTrue(body.asString == "")
    } catch {
      case e: ConnectException =>
        skipTestWarning("HEAD", testScriptUrl, e)
    }
  }

  def htmlGet(url: String) {
    try {
      val response = http.get(new URL(url))
      assertEquals(200, response.status.code)
      val body = response.body
      assertEquals(MediaType.TEXT_HTML, body.contentType)
      val string = body.asString
      assertTrue(string.startsWith("<html>"))
      val bodyLines = string.split("\n")
      assertEquals("GET", extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testScriptUrl, e)
    }
  }

  @Test
  def htmlGetOK() {
    htmlGet(testScriptUrl)
  }

  @Test
  def htmlGetOK2() {
    htmlGet(testScriptUrl + "?LOREM=1")
  }

  @Test
  def pngGetOK() {
    try {
      val response = http.get(new URL(testImageUrl))
      assertEquals(200, response.status.code)
      assertEquals(MediaType.IMAGE_PNG, response.body.contentType)
      val bytes = response.body.asInstanceOf[CachedBody].asBytes
      assertEquals(497, bytes.length)
      assertEquals('P', bytes(1))
      assertEquals('N', bytes(2))
      assertEquals('G', bytes(3))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testImageUrl, e)
    }
  }

  @Test
  def plainGetOK() {
    val url = testScriptUrl + "?CT=text/plain"
    try {
      val response = http.get(new URL(url))
      assertEquals(200, response.status.code)
      val body = response.body
      assertEquals(MediaType.TEXT_PLAIN, body.contentType)
      assertTrue(body.asString.startsWith("CONTENT_LENGTH"))
      val bodyLines = body.asString.split("\n")
      assertEquals("GET", extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", url, e)
    }
  }

  @Test
  def htmlPostWithJsonBodyOK() {
    val url = testScriptUrl + "?CT=text/plain"
    try {
      val response = http.post(new URL(url), jsonBody)
      assertEquals(302, response.status.code)
      val body = response.body
      assertEquals(MediaType.TEXT_HTML, body.contentType)
      assertEquals(0, body.asString.length)
      val location = response.headers.get(HeaderName.LOCATION).value
      assertTrue(location, location.startsWith(serverUrl))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testScriptUrl, e)
    }
  }

  @Test
  def htmlPostWithShortBodyOK() {
    val url = testScriptUrl + "?CT=text/plain"
    try {
      val response = http.post(new URL(url), jsonBody)
      assertEquals(302, response.status.code)
      val body = response.body
      assertEquals(MediaType.TEXT_HTML, body.contentType)
      assertEquals(0, body.asString.length)
      val location = response.headers.get(HeaderName.LOCATION).value
      assertTrue(location, location.startsWith(serverUrl))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testScriptUrl, e)
    }
  }

  @Test
  def htmlDeleteOK() {
    try {
      val response = http.delete(new URL(testScriptUrl + "?D=1"))
      assertEquals(200, response.status.code)
      val body = response.body
      assertEquals(MediaType.TEXT_HTML, body.contentType)
      assertTrue(body.asString.startsWith("<html>"))
      val bodyLines = body.asString.split("\n")
      assertEquals("DELETE", extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: ConnectException =>
        skipTestWarning("DELETE", testScriptUrl, e)
    }
  }

  @Test
  def soakTestJpgOK() {
    try {
      val size = 1605218
      val loops = 500
      val before = System.currentTimeMillis()
      for (i <- 1 to loops) {
        val response = http.get(new URL(testPhotoUrl + "?n=" + i))
        assertEquals(200, response.status.code)
        assertEquals(MediaType.IMAGE_JPG, response.body.contentType)
        val bytes = response.body.asInstanceOf[CachedBody].asBytes
        assertEquals(size, bytes.length)
      }
      val duration = System.currentTimeMillis() - before
      val bytes = BigDecimal(size * loops)
      val rate = (bytes / duration)
      println(bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testPhotoUrl, e)
    }
  }

  @Test
  def soakTestTextOK() {
    try {
      var size = -1
      var first = "NOT SET"
      val loops = 200
      val before = System.currentTimeMillis()
      for (i <- 1 to loops) {

        try {
          val is = i.toString
          val response = http.get(new URL(testScriptUrl + "?STUM=1"))
          assertEquals(is, 200, response.status.code)
          val body = response.body
          assertEquals(is, MediaType.TEXT_HTML, body.contentType)
          val string = body.asString
          assertTrue(is, string.startsWith("<html>"))
          if (size < 0) {
            first = string
            size = first.length
          } else {
            assertEquals(is, first, string)
            assertEquals(is, size, string.length)
          }
        } catch {
          case e: Exception =>
            throw new RuntimeException("soakTestTextOK " + i, e)
        }

      }
      val duration = System.currentTimeMillis() - before
      val bytes = BigDecimal(size * loops)
      val rate = (bytes / duration)
      println(bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testPhotoUrl, e)
    }
  }

  private def extractLineFromResponse(expectedHeader: String, bodyLines: Seq[String]): String = {
    val expectedHeaderColon = expectedHeader + ':'
    for (line <- bodyLines) {
      if (line.startsWith(expectedHeaderColon)) {
        return line.substring(line.indexOf(':') + 1).trim()
      }
    }
    throw new AssertionError("Expect response to contain\n" + expectedHeader)
  }

  private def skipTestWarning(method: String, url: String, e: ConnectException) {
    System.err.println("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
  }
}
