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

class CookieJarTest extends FunSuite {

  //TODO test for expiring cookies
  val ftpUrl1 = new URL("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL("http://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl2 = new URL("http://www.bbc.co.uk/radio/stations/radio1")
  val httpsUrl1 = new URL("https://www.w3.org/login/")
  val body = new StringResponseBody(MediaType.TEXT_PLAIN, "")
  val ok = Status(200, "OK")

  test("no cookies") {
    val tup = CookieJar.empty.gleanCookies(httpUrl1, Headers.empty)
    val newJar = tup._1.get
    assert(0 === newJar.size)
    assert(Headers.empty === tup._2)
  }

  test("simple construction") {
    val tenYears = Some(new HttpDateTimeInstant() + (10 * 365 * 24 * 60 * 60))
    val c1 = Cookie(name = "UID", value = "646f4472", domain = "bbc.co.uk", path = "/", expires = tenYears)
    val c2 = Cookie(name = "LANG", value = "en", domain = "www.bbc.co.uk", path = "/", expires = tenYears)
    val c3 = Cookie(name = "XYZ", value = "zzz", domain = "x.org", path = "/", expires = tenYears)

    val newJar = CookieJar(c1, c2, c3)
    assert(3 === newJar.size)
    assert(c1 === newJar.cookies(0))
    assert(c2 === newJar.cookies(1))
    assert(c3 === newJar.cookies(2))
  }

  test("parse plain cookie") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en")
    val tup = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))
    val newJar = tup._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "www.w3.org", "/standards/webdesign/") matches c1)
    assert("en" === c1.value)
    assert(false === c1.persistent)
    assert(Headers.empty === tup._2)
  }

  test("parse cookie with path") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Path=/standards")
    val newJar = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "www.w3.org", "/standards/") matches c1)
    assert("en" === c1.value)
  }

  test("parse cookie with domain") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Domain=w3.org")
    val newJar = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "w3.org", "/standards/webdesign/") matches c1)
    assert("en" === c1.value)
  }

  test("parse cookie with expiry") {
    val tomorrow = new HttpDateTimeInstant() + (24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Expires=" + tomorrow)
    val newJar = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "www.w3.org", "/standards/webdesign/") matches c1)
    assert("en" === c1.value)
    assert(tomorrow.seconds === c1.expires.get.seconds)
  }

  test("parse cookie with max age") {
    val day = 24 * 60 * 60
//    val tomorrow = new HttpDateTimeInstant() + day
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Max-Age=" + day)
    val newJar = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "www.w3.org", "/standards/webdesign/") matches c1)
    assert("en" === c1.value)
    assert(day === c1.maxAge.get)
  }

  test("parse cookie deletion") {
    val earlier = new HttpDateTimeInstant() - 1
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Expires=" + earlier)
    val h2 = HeaderName.SET_COOKIE -> ("foo=bar")
    val c1 = Cookie("lang", "en", "www.w3.org", "/standards/webdesign/")
    val key3 = CookieKey("foo", "www.w3.org", "/standards/webdesign/")
    val oldJar = CookieJar(c1)
    val newJar = oldJar.gleanCookies(httpUrl1, Headers(h1, h2))._1.get
    assert(1 === newJar.size)
    assert(newJar.contains(key3))
  }

  test("parse cookie with max age and expires") {
    val day1 = 24 * 60 * 60
    val day7 = day1 * 10
    val tomorrow = new HttpDateTimeInstant() + day1
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Max-Age=" + day7 + "; Expires=" + tomorrow)
    val newJar = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "www.w3.org", "/standards/webdesign/") matches c1)
    assert("en" === c1.value)
    assert(day7 === c1.maxAge.get)
    assert(tomorrow.seconds === c1.expires.get.seconds)
  }

  test("parse cookie with http only") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; HttpOnly")
    val newJar = CookieJar.empty.gleanCookies(ftpUrl1, Headers(h1))._1.get
    assert(0 === newJar.size)
  }

  test("parse cookie with secure") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Secure")
    val newJar = CookieJar.empty.gleanCookies(httpUrl1, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("lang", "www.w3.org", "/standards/webdesign/") matches c1)
    assert("en" === c1.value)
    assert(c1.secure)
  }

  test("parse one realistic cookie") {
    val tenYears = new HttpDateTimeInstant() + (10 * 365 * 24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("BBC-UID=646f4472; expires=" + tenYears + "; path=/; domain=bbc.co.uk")
    val newJar = CookieJar.empty.gleanCookies(httpUrl2, Headers(h1))._1.get
    assert(1 === newJar.size)
    val c1 = newJar.cookies.iterator.next()
    assert(CookieKey("BBC-UID", "bbc.co.uk", "/") matches c1)
    assert("646f4472" === c1.value)
    assert(false === c1.secure)
    assert(false === c1.httpOnly)
    assert(false === c1.hostOnly)
    assert(c1.persistent)
    assert(tenYears.seconds === c1.expires.get.seconds)
  }

  test("parse realistic cookie list") {
    val c1Str = "c1=v1; path=/; domain=.z.com"
    val c2Str = "cc2=vv2; path=/; domain=.z.com; expires=Sun, 12-May-2013 11:21:33 GMT"
    val c3Str = "ccc3=f1=5; path=/; domain=.z.com; expires=Mon, 12-Sep-2022 11:21:33 GMT"
    val h1 = HeaderName.SET_COOKIE -> (c1Str + "\n" + c2Str + "\n" + c3Str)

    val newJar = CookieJar.empty.gleanCookies(httpUrl2, Headers(h1))._1.get
    assert(3 === newJar.size)
    val k1 = CookieKey("c1", "z.com", "/")
    val k2 = CookieKey("cc2", "z.com", "/")
    val k3 = CookieKey("ccc3", "z.com", "/")
    assert(newJar contains k1)
    assert(newJar contains k2)
    assert(newJar contains k3)
    val c1 = newJar.get(k1).get
    val c2 = newJar.get(k2).get
    val c3 = newJar.get(k3).get
    assert("v1" === c1.value)
    assert("vv2" === c2.value)
    assert("f1=5" === c3.value)
  }

  test("filter for request with two cookies") {
    val tenYears = Some(10 * 365 * 24 * 60 * 60)
    val cKey1 = CookieKey("UID", "bbc.co.uk", "/")
    val cVal1 = cKey1 ->("646f4472", tenYears)

    val cKey2 = CookieKey("LANG", "www.bbc.co.uk", "/")
    val cVal2 = cKey2 ->("en", tenYears)

    val cKey3 = CookieKey("XYZ", "x.org", "/")
    val cVal3 = cKey3 ->("zzz", tenYears)

    val jar = CookieJar(cVal1, cVal2, cVal3)

    val header = jar.filterForRequest(httpUrl2).get
    assert(HeaderName.COOKIE.name === header.name)
    assert(true, header.value === header.value == "UID=646f4472; LANG=en" || header.value == "LANG=en; UID=646f4472")
  }

  //  test("merge") {
  //    val year = new HttpDateTimeInstant() + (365 * 24 * 60 * 60)
  //
  //    // case 1: value to be updated
  //    val cKey1 = CookieKey("k1", "x.org", "/")
  //    val cVal1a = CookieValue(string = "646f4472", expires = year)
  //    val cVal1b = CookieValue(string = "12345678", expires = year)
  //
  //    // case 2: no change for differing keys
  //    val cKey2a = CookieKey("k2", "x.org", "/somewhere/")
  //    val cVal2a = CookieValue(string = "en", expires = year)
  //    val cKey2b = CookieKey("k2", "x.org", "/else/")
  //    val cVal2b = CookieValue(string = "fr", expires = year)
  //
  //    // untouched
  //    val cKey3 = CookieKey("k3", "x.org", "/")
  //    val cVal3 = CookieValue(string = "aaa", expires = year)
  //
  //    val cKey4 = CookieKey("k4", "x.org", "/")
  //    val cVal4 = CookieValue(string = "bbb", expires = year)
  //
  //    val oldJar = new CookieJar(ListMap(cKey1 -> cVal1a, cKey2a -> cVal2a, cKey3 -> cVal3), Set(cKey4))
  //    val newJar = new CookieJar(ListMap(cKey1 -> cVal1b, cKey2b -> cVal2b, cKey4 -> cVal4))
  //
  //    val merged = oldJar.merge(newJar)
  //    assert(5 === merged.size)
  //    assert("12345678" === merged.cookieMap.get(cKey1).get.value)
  //    assert("en" === merged.cookieMap.get(cKey2a).get.value)
  //    assert("fr" === merged.cookieMap.get(cKey2b).get.value)
  //    assert("aaa" === merged.cookieMap.get(cKey3).get.value)
  //    assert("bbb" === merged.cookieMap.get(cKey4).get.value)
  //  }

  test("add and remove") {
    val cKey1 = CookieKey("k1", "x.org", "/")
    val cKey2 = CookieKey("k2", "x.org", "/somewhere/")
    val cKey3 = CookieKey("k3", "x.org", "/")
    val cKey4 = CookieKey("k4", "x.org", "/")

    val cVal1 = cKey1 -> ("a1")
    val cVal2 = cKey2 -> ("b2")
    val cVal3 = cKey3 -> ("c3")

    val oldJar = CookieJar(cVal1)
    val expandedJar1 = oldJar + cVal2
    assert("a1" === expandedJar1.get(cKey1).get.value)
    assert("b2" === expandedJar1.get(cKey2).get.value)
    assert(false === expandedJar1.contains(cKey3))

    val expandedJar2 = expandedJar1 + cVal3
    assert("a1" === expandedJar2.get(cKey1).get.value)
    assert("b2" === expandedJar2.get(cKey2).get.value)
    assert("c3" === expandedJar2.get(cKey3).get.value)

    val alteredJar = expandedJar2 + (cKey3 -> ("b2"))
    assert("a1" === alteredJar.get(cKey1).get.value)
    assert("b2" === alteredJar.get(cKey2).get.value)
    assert("b2" === alteredJar.get(cKey3).get.value)

    val reducedJar1 = alteredJar - cKey3
    assert(false === reducedJar1.contains(cKey3))

    //    val reducedJar2 = reducedJar1 - cKey4
    //    assert(false === reducedJar2.deleted.contains(cKey4))
  }

  test("url behaviour") {
    val url1 = new URL("http://me@w3.org:8000/some/path/file.html?q=1#aaa")
    assert("http" === url1.getProtocol)
    assert("w3.org" === url1.getHost)
    assert(8000 === url1.getPort)
    assert(80 === url1.getDefaultPort)
    assert("/some/path/file.html" === url1.getPath)
    assert("/some/path/file.html?q=1" === url1.getFile)
    assert("q=1" === url1.getQuery)
    assert("aaa" === url1.getRef)
    assert("me" === url1.getUserInfo)

    val url2 = new URL("https://w3.org/")
    assert("https" === url2.getProtocol)
    assert("w3.org" === url2.getHost)
    assert(-1 === url2.getPort)
    assert(443 === url2.getDefaultPort)
    assert("/" === url2.getPath)
    assert("/" === url2.getFile)
    expect(null)(url2.getQuery)
    expect(null)(url2.getRef)
    expect(null)(url2.getUserInfo)
  }

  test("filter cookies by name") {
    val tenYears = Some(10 * 365 * 24 * 60 * 60)
    val cKey1 = CookieKey("X1", "bbc.co.uk", "/")
    val cVal1 = cKey1 ->("root", tenYears)

    val cKey2 = CookieKey("X1", "bbc.co.uk", "/subdir/")
    val cVal2 = cKey2 ->("subdir", tenYears)

    val cKey3 = CookieKey("X1", "x.org", "/")
    val cVal3 = cKey3 ->("zzz", tenYears)

    val cKey4 = CookieKey("X2", "x.org", "/")
    val cVal4 = cKey4 ->("aaa", tenYears)

    val jar = CookieJar(cVal1, cVal2, cVal3, cVal4)

    val foundX1 = jar.find(_.name == "X1")
    assert(foundX1.isDefined)
    assert("root" === foundX1.get.value)

    val filteredX1 = jar.filter(_.name == "X1").toList
    assert(3 === filteredX1.size)
    assert("root" === filteredX1(0).value)
    assert("subdir" === filteredX1(1).value)
    assert("zzz" === filteredX1(2).value)

    val filteredX2 = jar.filter(_.name == "X2").toList
    assert(1 === filteredX2.size)
    assert("aaa" === filteredX2(0).value)

    val zipped = jar.cookies.toList
    assert(4 === zipped.size)
    assert("root" === zipped(0).value)
    assert("subdir" === zipped(1).value)
    assert("zzz" === zipped(2).value)
    assert("aaa" === zipped(3).value)
  }
}
