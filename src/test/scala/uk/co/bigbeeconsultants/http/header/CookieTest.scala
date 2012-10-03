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

package uk.co.bigbeeconsultants.http.header

import java.net.URL
import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http.HttpDateTimeInstant
import java.util.Date

class CookieTest extends FunSuite {

  val ftpUrl1 = new URL("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL("http://www.w3.org/standards/webdesign/htmlcss")
  val httpsUrl1 = new URL("https://www.w3.org/login/")


  test("cookie_matches_path") {
    val w3root = CookieKey("n1", "www.w3.org")
    val w3standards = CookieKey("n1", Domain("www.w3.org"), "/standards/")
    val c1 = w3root -> ("v1")
    val c2 = w3standards -> ("v2")
    assert(true === c1.willBeSentTo(httpUrl1))
    assert(true === c1.willBeSentTo(httpsUrl1))
    assert(true === c2.willBeSentTo(httpUrl1))
    assert(false === c2.willBeSentTo(httpsUrl1))
  }


  test("cookie_matches_secure") {
    val w3 = CookieKey("n1", "www.w3.org")
    val c1 = (w3 -> ("v1")).copy(secure = false)
    val c2 = (w3 -> ("v2")).copy(secure = true)
    assert(true === c1.willBeSentTo(httpUrl1))
    assert(true === c1.willBeSentTo(httpsUrl1))
    assert(false === c2.willBeSentTo(httpUrl1))
    assert(true === c2.willBeSentTo(httpsUrl1))
  }


  test("cookie_matches_httpOnly") {
    val w3 = CookieKey("n1", "www.w3.org")
    val c1 = (w3 -> ("v1")).copy(httpOnly = false)
    val c2 = (w3 -> ("v2")).copy(httpOnly = true)
    assert(true === c1.willBeSentTo(ftpUrl1))
    assert(false === c2.willBeSentTo(ftpUrl1))
    assert(true === c2.willBeSentTo(httpUrl1))
    assert(true === c2.willBeSentTo(httpUrl1))
    assert(true === c2.willBeSentTo(httpsUrl1))
    assert(true === c2.willBeSentTo(httpsUrl1))
  }


  test("cookie_matches_domain") {
    val w3 = CookieKey("n1", "www.w3.org")
    val xorg = CookieKey("n1", "x.org")
    val c1 = w3 -> ("v1")
    val c2 = xorg -> ("v2")
    assert(true === c1.willBeSentTo(httpUrl1))
    assert(false === c2.willBeSentTo(httpUrl1))
    assert(true === c1.willBeSentTo(httpsUrl1))
    assert(false === c2.willBeSentTo(httpsUrl1))
  }


  test("conversion to servlet cookie") {
    val now = new HttpDateTimeInstant
    val now20 = new HttpDateTimeInstant(new Date(System.currentTimeMillis() + 20000))
    val c = Cookie("n1", "v1", "www.w3.org", "/path/", Some(10), Some(now20), now, false, false, false, false, "http")
    val s = c.asServletCookie
    assert("n1" === s.getName)
    assert("v1" === s.getValue)
    assert("/path/" === s.getPath)
    assert("www.w3.org" === s.getDomain)
    assert(10 === s.getMaxAge)
  }
}
