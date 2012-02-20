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
import java.util.Date
import uk.co.bigbeeconsultants.lhc.HttpDate
import org.junit.Assert._
import uk.co.bigbeeconsultants.lhc.header.Cookie._

class CookieTest {

  val ftpUrl1 = new URL("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL("http://www.w3.org/standards/webdesign/htmlcss")
  val httpsUrl1 = new URL("https://www.w3.org/login/")

  @Test
  def cookie_matches_path() {
    val w3root = CookieKey("n1", Domain("www.w3.org"))
    val w3standards = CookieKey("n1", Domain("www.w3.org"), "/standards/")
    val c1 = Cookie(w3root, new CookieValue(value = "v1"))
    val c2 = Cookie(w3standards, new CookieValue(value = "v2"))
    assertTrue(c1.willBeSentTo(httpUrl1))
    assertTrue(c1.willBeSentTo(httpsUrl1))
    assertTrue(c2.willBeSentTo(httpUrl1))
    assertFalse(c2.willBeSentTo(httpsUrl1))
  }

  @Test
  def cookie_matches_secure() {
    val w3 = CookieKey("n1", Domain("www.w3.org"))
    val c1 = Cookie(w3, new CookieValue(value = "v1", secure = false))
    val c2 = Cookie(w3, new CookieValue(value = "v2", secure = true))
    assertTrue(c1.willBeSentTo(httpUrl1))
    assertTrue(c1.willBeSentTo(httpsUrl1))
    assertFalse(c2.willBeSentTo(httpUrl1))
    assertTrue(c2.willBeSentTo(httpsUrl1))
  }

  @Test
  def cookie_matches_httpOnly() {
    val w3 = CookieKey("n1", Domain("www.w3.org"))
    val c1 = Cookie(w3, new CookieValue(value = "v1", httpOnly = false))
    val c2 = Cookie(w3, new CookieValue(value = "v2", httpOnly = true))
    assertTrue(c1.willBeSentTo(ftpUrl1))
    assertFalse(c2.willBeSentTo(ftpUrl1))
    assertTrue(c2.willBeSentTo(httpUrl1))
    assertTrue(c2.willBeSentTo(httpUrl1))
    assertTrue(c2.willBeSentTo(httpsUrl1))
    assertTrue(c2.willBeSentTo(httpsUrl1))
  }

  @Test
  def cookie_matches_domain() {
    val w3 = CookieKey("n1", Domain("www.w3.org"))
    val xorg = CookieKey("n1", Domain("x.org"))
    val c1 = Cookie(w3, new CookieValue(value = "v1"))
    val c2 = Cookie(xorg, new CookieValue(value = "v2"))
    assertTrue(c1.willBeSentTo(httpUrl1))
    assertFalse(c2.willBeSentTo(httpUrl1))
    assertTrue(c1.willBeSentTo(httpsUrl1))
    assertFalse(c2.willBeSentTo(httpsUrl1))
  }
}