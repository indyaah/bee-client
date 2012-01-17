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
import java.net.URL

object HttpIntegration {

  private val testScriptUrl = "http://localhost/test-lighthttpclient.php"

  def htmlHeadOK(http: HttpClient) {
    val response = http.head(new URL(testScriptUrl))
    assertEquals(200, response.status.code)
    assertEquals(MediaType.TEXT_HTML, response.contentType)
    val body = response.body
    assertTrue(body.startsWith(""))
  }

  def htmlGetOK(http: HttpClient) {
    val response = http.get(new URL(testScriptUrl))
    assertEquals(200, response.status.code)
    assertEquals(MediaType.TEXT_HTML, response.contentType)
    val body = response.body
    assertTrue(body.startsWith("<html>"))
  }

  def plainGetOK(http: HttpClient) {
    val response = http.get(new URL(testScriptUrl + "?CT=text/plain"))
    assertEquals(200, response.status.code)
    assertEquals(MediaType.TEXT_PLAIN, response.contentType)
    val body = response.body
    println( response.headers )
    println( body )
    assertTrue(body.startsWith("CONTENT_"))
  }


  def main(args: Array[String]) {
    val http = new HttpClient(false)
//    htmlGetOK(http)
    plainGetOK(http)
//    htmlHeadOK(http)
    http.closeConnections()
  }
}