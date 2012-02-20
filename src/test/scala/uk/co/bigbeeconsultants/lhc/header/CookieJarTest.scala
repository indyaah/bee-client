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

package uk.co.bigbeeconsultants.lhc.header

import org.junit.Test
import java.net.URL
import org.junit.Assert._
import uk.co.bigbeeconsultants.lhc.request.Request
import uk.co.bigbeeconsultants.lhc.response.{Status, Response, Body, StringBodyCache}

class CookieJarTest {

  val ftpUrl1 = new URL("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL("http://www.w3.org/standards/webdesign/htmlcss")
  val httpsUrl1 = new URL("https://www.w3.org/login/")
  val body = new StringBodyCache(MediaType.TEXT_PLAIN, "")

  @Test
  def parse() {
    val h1 = HeaderName.SET_COOKIE -> "lang=en-US; Expires=Wed, 09 Jun 2021 10:18:14 GMT"
    val h2 = HeaderName.SET_COOKIE -> "lang=; Expires=Sun, 06 Nov 1994 08:49:37 GMT"
    val request = Request.get(httpUrl1)
    val response = Response(request, Status(200, "OK"), body, Headers(List(h1)))
    val jar = new CookieJar
    val newJar = jar.updateCookies(response)
  }

  @Test
  def cookieJar_filterByUrl1() {
  }
}
