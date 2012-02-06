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

import java.net.{ConnectException, URL}
import java.lang.AssertionError
import org.junit.Assert._
import org.junit.{After, Before, Test}

class HttpIntegration {

  private val serverUrl = "http://localhost/lighthttpclient/"
  private val testScriptUrl = serverUrl + "test-lighthttpclient.php"
  private val testImageUrl = serverUrl + "B.png"
  private val jsonBody = RequestBody(MediaType.APPLICATION_JSON, """{ "x": 1, "y": true }""")

  var http: HttpClient = _

  @Before
  def before() {
    http = new HttpClient(keepAlive = false,
      requestConfig = HttpClient.defaultRequestConfig.copy(followRedirects = false))
  }

  @After
  def close() {
    http.closeConnections()
  }

  @Test
  def htmlHeadOK() {
    try {
      val response = http.head(new URL(testScriptUrl))
      assertEquals(200, response.status.code)
      assertEquals(MediaType.TEXT_HTML, response.contentType)
      val body = response.body
      assertTrue(body == "")
    } catch {
      case e: ConnectException =>
        skipTestWarning("HEAD", testScriptUrl, e)
    }
  }

  @Test
  def htmlGetOK() {
    try {
      val response = http.get(new URL(testScriptUrl))
      assertEquals(200, response.status.code)
      assertEquals(MediaType.TEXT_HTML, response.contentType)
      val body = response.body
      assertTrue(body.startsWith("<html>"))
      val bodyLines = body.split("\n")
      assertEquals("GET", extractLineFromResponse("REQUEST_METHOD", bodyLines))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testScriptUrl, e)
    }
  }

  @Test
  def pngGetOK() {
    try {
      val response = http.get(new URL(testImageUrl))
      assertEquals(200, response.status.code)
      assertEquals(MediaType.IMAGE_PNG, response.contentType)
      val bytes = response.bodyAsBytes
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
      assertEquals(MediaType.TEXT_PLAIN, response.contentType)
      val body = response.body
      //println(response.headers)
      //println(body)
      assertTrue(body.startsWith("CONTENT_LENGTH"))
      val bodyLines = body.split("\n")
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
      assertEquals(MediaType.TEXT_HTML, response.contentType)
      val body = response.body
      assertEquals(0, body.length)
      val location = response.headers("LOCATION").value
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
      assertEquals(MediaType.TEXT_HTML, response.contentType)
      val body = response.body
      assertEquals(0, body.length)
      val location = response.headers("LOCATION").value
      assertTrue(location, location.startsWith(serverUrl))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", testScriptUrl, e)
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
