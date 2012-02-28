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

import header.{HeaderName, MediaType}
import java.net.{ConnectException, URL}
import java.lang.AssertionError
import request.{Config, Body}
import org.scalatest.{BeforeAndAfter, FunSuite}

class HttpIntegration extends FunSuite with BeforeAndAfter {

  private val serverUrl = "http://localhost/lighthttpclient/"
  private val testScriptUrl = serverUrl + "test-lighthttpclient.php"
  private val testImageUrl = serverUrl + "B.png"
  private val testPhotoUrl = serverUrl + "plataria-sunset.jpg"
  private val jsonBody = Body (MediaType.APPLICATION_JSON, """{ "x": 1, "y": true }""")

  var http: HttpClient = _

  before {
    http = new HttpClient (config = Config (followRedirects = false))
  }

  after {
    http.closeConnections ()
  }

  test ("setupOK") {
    //    assertFalse(http.config.keepAlive)
  }

  test ("htmlHeadOK") {
    try {
      val response = http.head (new URL (testScriptUrl))
      expect (200)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expect (true)(body.toString == "")
    } catch {
      case e: Exception =>
        skipTestWarning ("HEAD", testScriptUrl, e)
    }
  }

  private def htmlGet(url: String) {
    try {
      val response = http.get (new URL (url))
      expect (200)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      val string = body.toString
      expect (true)(string.startsWith ("<html>"))
      val bodyLines = string.split ("\n")
      expect ("GET")(extractLineFromResponse ("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testScriptUrl, e)
    }
  }

  test ("htmlGetOK") {
    htmlGet (testScriptUrl)
  }

  test ("htmlGetOK2") {
    htmlGet (testScriptUrl + "?LOREM=1")
  }

  test ("pngGetOK") {
    try {
      val response = http.get (new URL (testImageUrl))
      expect (200)(response.status.code)
      expect (MediaType.IMAGE_PNG)(response.body.contentType)
      val bytes = response.body.asBytes
      expect (497)(bytes.length)
      expect ('P')(bytes (1))
      expect ('N')(bytes (2))
      expect ('G')(bytes (3))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testImageUrl, e)
    }
  }

  test ("plainGetOK") {
    val url = testScriptUrl + "?CT=text/plain"
    try {
      val response = http.get (new URL (url))
      expect (200)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_PLAIN)(body.contentType)
      expect (true)(body.toString.startsWith ("CONTENT_LENGTH"))
      val bodyLines = body.toString.split ("\n")
      expect ("GET")(extractLineFromResponse ("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", url, e)
    }
  }

  test ("htmlPostWithJsonBodyOK") {
    val url = testScriptUrl + "?CT=text/plain"
    try {
      val response = http.post (new URL (url), jsonBody)
      expect (302)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expect (0)(body.toString.length)
      val location = response.headers.get (HeaderName.LOCATION).value
      expect (true, location)(location.startsWith (serverUrl))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testScriptUrl, e)
    }
  }

  test ("htmlPostWithShortBodyOK") {
    val url = testScriptUrl + "?CT=text/plain"
    try {
      val response = http.post (new URL (url), jsonBody)
      expect (302)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expect (0)(body.toString.length)
      val location = response.headers.get (HeaderName.LOCATION).value
      expect (true, location)(location.startsWith (serverUrl))
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testScriptUrl, e)
    }
  }

  test ("htmlDeleteOK") {
    try {
      val response = http.delete (new URL (testScriptUrl + "?D=1"))
      expect (200)(response.status.code)
      val body = response.body
      expect (MediaType.TEXT_HTML)(body.contentType)
      expect (true)(body.toString.startsWith ("<html>"))
      val bodyLines = body.toString.split ("\n")
      expect ("DELETE")(extractLineFromResponse ("REQUEST_METHOD", bodyLines))
    } catch {
      case e: Exception =>
        skipTestWarning ("DELETE", testScriptUrl, e)
    }
  }

  test ("soakTestJpgOK") {
    try {
      val size = 1605218
      val loops = 500
      val before = System.currentTimeMillis ()
      for (i <- 1 to loops) {
        val response = http.get (new URL (testPhotoUrl + "?n=" + i))
        expect (200)(response.status.code)
        expect (MediaType.IMAGE_JPG)(response.body.contentType)
        val bytes = response.body.asBytes
        expect (size)(bytes.length)
      }
      val duration = System.currentTimeMillis () - before
      val bytes = BigDecimal (size * loops)
      val rate = (bytes / duration)
      println (bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testPhotoUrl, e)
    }
  }

  test ("soakTestTextOK") {
    try {
      var size = -1
      var first = "NOT SET"
      val loops = 200
      val before = System.currentTimeMillis ()
      var ok = true
      for (i <- 1 to loops) {
        if (ok) {
          try {
            val is = i.toString
            val response = http.get (new URL (testScriptUrl + "?STUM=1"))
            expect (200)(response.status.code)
            val body = response.body
            expect (MediaType.TEXT_HTML)(body.contentType)
            val string = body.toString
            expect (true, is)(string.startsWith ("<html>"))
            if (size < 0) {
              first = string
              size = first.length
            } else {
              expect (first, is)(string)
              expect (size, is)(string.length)
            }
          } catch {
            case e: Exception =>
              skipTestWarning ("GET", "soakTestTextOK " + i, e)
              ok = false
          }
        }
      }

      val duration = System.currentTimeMillis () - before
      val bytes = BigDecimal (size * loops)
      val rate = (bytes / duration)
      println (bytes + " bytes took " + duration + "ms at " + rate + " kbyte/sec")
    } catch {
      case e: Exception =>
        skipTestWarning ("GET", testPhotoUrl, e)
    }
  }

  private def extractLineFromResponse(expectedHeader: String, bodyLines: Seq[String]): String = {
    val expectedHeaderColon = expectedHeader + ':'
    for (line <- bodyLines) {
      if (line.startsWith (expectedHeaderColon)) {
        return line.substring (line.indexOf (':') + 1).trim ()
      }
    }
    throw new AssertionError ("Expect response to contain\n" + expectedHeader)
  }

  private def skipTestWarning(method: String, url: String, e: Exception) {
    if (e.getCause.isInstanceOf[ConnectException]) {
      System.err.println ("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
    }
    else {
      throw e
    }
  }
}
