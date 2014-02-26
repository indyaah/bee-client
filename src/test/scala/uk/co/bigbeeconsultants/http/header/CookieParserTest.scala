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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CookieParserTest extends FunSuite {

  test("example 1: name, value") {
    val str = "SID=31d4d96e407aad42"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://a.z.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.size === 1)
    val cookie = cookies(0)
    assert(CookieParser.asSetHeader(cookie).startsWith(str))
    assert(cookie.name === "SID")
    assert(cookie.value === "31d4d96e407aad42")
    assert(cookie.domain.domain === "a.z.com")
    assert(cookie.path === "/animal/")
    assert(cookie.serverProtocol === "http")
    assert(cookie.expires === None)
    assert(cookie.maxAge === None)
    assert(!cookie.persistent)
    assert(cookie.hostOnly)
    assert(!cookie.secure)
    assert(!cookie.httpOnly)
  }

  test("example 2: name, value, expires, httponly, max-age, path, secure") {
    val str = "session=c47666897a224a93add9; expires=Wed, 19-Feb-2034 20:13:23 GMT; httponly; Max-Age=1209600; Path=/; secure"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://a.z.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.size === 1)
    val cookie = cookies(0)
    assert(cookie.name === "session")
    assert(cookie.value === "c47666897a224a93add9")
    assert(cookie.domain.domain === "a.z.com")
    assert(cookie.path === "/")
    assert(cookie.serverProtocol === "http")
    assert(cookie.expires.get === HttpDateTimeInstant.parse("Wed, 19-Feb-2034 20:13:23 GMT"))
    assert(cookie.maxAge === Some(1209600))
    assert(cookie.persistent)
    assert(cookie.hostOnly)
    assert(cookie.secure)
    assert(cookie.httpOnly)
//    assert(CookieParser.asSetHeader(cookie).toLowerCase === str.toLowerCase)
  }

  test("example 3: name, value, domain, expires, httponly, secure") {
    val str = "logged_in=yes; domain=.frodo.com; path=/; expires=Sun, 05-Feb-2034 20:23:23 GMT; secure; HttpOnly"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://a.frodo.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.size === 1)
    val cookie = cookies(0)
    assert(cookie.name === "logged_in")
    assert(cookie.value === "yes")
    assert(cookie.domain.domain === "frodo.com")
    assert(cookie.path === "/")
    assert(cookie.serverProtocol === "http")
    assert(cookie.expires === Some(HttpDateTimeInstant.parse("Sun, 05-Feb-2034 20:23:23 GMT")))
    assert(cookie.maxAge === None)
    assert(cookie.persistent)
    assert(!cookie.hostOnly)
    assert(cookie.secure)
    assert(cookie.httpOnly)
//    assert(CookieParser.asSetHeader(cookie).toLowerCase === str.toLowerCase)
  }

  test("example 4: name, value, path, expires, httponly, secure") {
    val str = "user_session=rTIepD313gqaAEvLbBZvIWOGHMTb3CyPp3YTlxeVf0kd; path=/; expires=Wed, 19-Feb-2034 20:23:23 GMT; secure; HttpOnly"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://a.z.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.size === 1)
    val cookie = cookies(0)
    assert(cookie.name === "user_session")
    assert(cookie.value === "rTIepD313gqaAEvLbBZvIWOGHMTb3CyPp3YTlxeVf0kd")
    assert(cookie.domain.domain === "a.z.com")
    assert(cookie.path === "/")
    assert(cookie.serverProtocol === "http")
    assert(cookie.expires === Some(HttpDateTimeInstant.parse("Wed, 19-Feb-2034 20:23:23 GMT")))
    assert(cookie.maxAge === None)
    assert(cookie.persistent)
    assert(cookie.hostOnly)
    assert(cookie.secure)
    assert(cookie.httpOnly)
//    assert(CookieParser.asSetHeader(cookie).toLowerCase === str.toLowerCase)
  }

  test("example 5: name, value, path, httponly, secure") {
    val str = "_gh_sess=BAh7BjoPc2Vzc2lvbl9pZEkiJWIwODQ2YTFhMThmY2YzYjU3MDVlOGJjY2RmMWJmYjlkBjoGRUY%3D--afafe028ab4b30196967becb2c7e13666b149219; path=/; secure; HttpOnly"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://a.z.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.size === 1)
    val cookie = cookies(0)
    assert(cookie.name === "_gh_sess")
    assert(cookie.value === "BAh7BjoPc2Vzc2lvbl9pZEkiJWIwODQ2YTFhMThmY2YzYjU3MDVlOGJjY2RmMWJmYjlkBjoGRUY%3D--afafe028ab4b30196967becb2c7e13666b149219")
    assert(cookie.domain.domain === "a.z.com")
    assert(cookie.path === "/")
    assert(cookie.serverProtocol === "http")
    assert(cookie.expires === None)
    assert(cookie.maxAge === None)
    assert(!cookie.persistent)
    assert(cookie.hostOnly)
    assert(cookie.secure)
    assert(cookie.httpOnly)
//    assert(CookieParser.asSetHeader(cookie).toLowerCase === str.toLowerCase)
  }

  test("example 6: name, value, domain, path, expires, httponly, secure") {
    val str = "dotcom_user=frodo123; domain=.frodo.com; path=/chat; expires=Sun, 05-Feb-2034 20:23:23 GMT; secure; HttpOnly"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://www.frodo.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.size === 1)
    val cookie = cookies(0)
    assert(cookie.name === "dotcom_user")
    assert(cookie.value === "frodo123")
    assert(cookie.domain.domain === "frodo.com")
    assert(cookie.path === "/chat/")
    assert(cookie.serverProtocol === "http")
    assert(cookie.expires === Some(HttpDateTimeInstant.parse("Sun, 05-Feb-2034 20:23:23 GMT")))
    assert(cookie.maxAge === None)
    assert(cookie.persistent)
    assert(!cookie.hostOnly)
    assert(cookie.secure)
    assert(cookie.httpOnly)
//    assert(CookieParser.asSetHeader(cookie).toLowerCase === str.toLowerCase)
  }

  test("httponly filter") {
    val str = "user=frodo123; domain=.frodo.com; path=/; expires=Sun, 05-Feb-2034 20:23:23 GMT; secure; HttpOnly"
    val headers = HeaderName.SET_COOKIE -> str
    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("ftp://www.frodo.com/animal/zebra.html"), Headers(headers))._1.get.cookies
    assert(cookies.isEmpty)
  }

  test("asSetHeader") {
    val c1Str = "c1=v1; Path=/; Domain=z.com"
    val c2Str = "cc2=vv2; Path=/; Domain=z.com; Expires=Thu, 12 May 2022 11:21:33 GMT"
    val c3Str = "ccc3=f1=5; Path=/; Domain=z.com; Expires=Mon, 12 Sep 2022 11:21:33 GMT"
    val h1 = HeaderName.SET_COOKIE -> (c1Str + "\n" + c2Str + "\n" + c3Str)

    val cookies: List[Cookie] = CookieJar.gleanCookies(Some(CookieJar.Empty), new URL("http://a.z.com/animal/zebra.html"), Headers(h1))._1.get.cookies
    val c1 = cookies(2)
    val c2 = cookies(1)
    val c3 = cookies(0)
    assert("c1" === c1.name)
    assert("cc2" === c2.name)
    assert("ccc3" === c3.name)
    assert(c1Str === CookieParser.asSetHeader(c1))
    assert(c2Str === CookieParser.asSetHeader(c2))
    assert(c3Str === CookieParser.asSetHeader(c3))
    //val cookies = for (i <- 1 to 100) yield new Cookie("n"+i, "v"+i)
  }

}
