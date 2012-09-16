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
import uk.co.bigbeeconsultants.http.response.{Status, StringResponseBody}
import uk.co.bigbeeconsultants.http.HttpDateTimeInstant
import org.scalatest.FunSuite
import collection.immutable.IndexedSeq

class CookieParserTest extends FunSuite {

  test("example 1") {
    val str = "SID=31d4d96e407aad42"
    val headers = HeaderName.SET_COOKIE -> (str)
    val cookies = CookieJar.empty.gleanCookies(new URL("http://a.z.com/"), Headers(headers)).cookies
    expect(1)(cookies.size)
    val cookie = cookies(0)
    assert(CookieParser.asSetHeader(cookie).startsWith(str))
    expect("SID")(cookie.name)
    expect("31d4d96e407aad42")(cookie.value)
    expect("a.z.com")(cookie.domain.domain)
    expect("/")(cookie.path)
    expect("http")(cookie.serverProtocol)
    expect(None)(cookie.expires)
    assert(!cookie.persistent)
    assert(cookie.hostOnly)
    assert(!cookie.secure)
    assert(!cookie.httpOnly)
  }

  test("asSetHeader") {
    val c1Str = "c1=v1; Path=/; Domain=z.com"
    val c2Str = "cc2=vv2; Path=/; Domain=z.com; Expires=Sun, 12 May 2013 11:21:33 GMT"
    val c3Str = "ccc3=f1=5; Path=/; Domain=z.com; Expires=Mon, 12 Sep 2022 11:21:33 GMT"
    val h1 = HeaderName.SET_COOKIE -> (c1Str + "\n" + c2Str + "\n" + c3Str)

    val cookies = CookieJar.empty.gleanCookies(new URL("http://a.z.com/"), Headers(h1)).cookies
    val c1 = cookies(2)
    val c2 = cookies(1)
    val c3 = cookies(0)
    expect("c1")(c1.name)
    expect("cc2")(c2.name)
    expect("ccc3")(c3.name)
    expect(c1Str)(CookieParser.asSetHeader(c1))
    expect(c2Str)(CookieParser.asSetHeader(c2))
    expect(c3Str)(CookieParser.asSetHeader(c3))
    //val cookies = for (i <- 1 to 100) yield new Cookie("n"+i, "v"+i)
  }
}
