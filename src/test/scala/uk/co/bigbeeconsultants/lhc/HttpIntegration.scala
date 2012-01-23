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

import org.junit.Assert._
import org.junit.{AfterClass, Test}
import java.net.{ConnectException, URL}

class HttpIntegration {

  private val serverUrl = "http://localhost/"
  private val testScriptUrl = serverUrl + "test-lighthttpclient.php"
  private val testImageUrl = serverUrl + "B.png"

  import HttpIntegration._

  @Test
  def htmlHeadOK() {
    try {
      val response = http.head(new URL(testScriptUrl))
      assertEquals(200, response.status.code)
      assertEquals(MediaType.TEXT_HTML, response.contentType)
      val body = response.body
      assertTrue(body.startsWith(""))
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
      println(response.headers)
      println(body)
      assertTrue(body.startsWith("CONTENT_"))
    } catch {
      case e: ConnectException =>
        skipTestWarning("GET", url, e)
    }
  }

  private def skipTestWarning(method: String, url: String, e: ConnectException) {
    System.err.println("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
  }
}

object HttpIntegration {
  val http = new HttpClient(false)

  @AfterClass
  def close() {
    http.closeConnections()
  }
}