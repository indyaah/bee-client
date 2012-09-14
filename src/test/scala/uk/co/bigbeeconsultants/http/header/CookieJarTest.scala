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
import collection.immutable.ListMap

class CookieJarTest extends FunSuite {

  val ftpUrl1 = new URL ("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL ("http://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl2 = new URL ("http://www.bbc.co.uk/radio/stations/radio1")
  val httpsUrl1 = new URL ("https://www.w3.org/login/")
  val body = new StringResponseBody (MediaType.TEXT_PLAIN, "")
  val ok = Status (200, "OK")

  test ("no cookies") {
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers.empty)
    expect (0)(newJar.cookieMap.size)
  }

  test ("simple construction") {
    val tenYears = new HttpDateTimeInstant () + (10 * 365 * 24 * 60 * 60)
    val c1 = Cookie (name = "UID", domain = "bbc.co.uk", path = "/", string = "646f4472", expires = tenYears)
    val c2 = Cookie (name = "LANG", domain = "www.bbc.co.uk", path = "/", string = "en", expires = tenYears)
    val c3 = Cookie (name = "XYZ", domain = "x.org", path = "/", string = "zzz", expires = tenYears)

    val newJar = CookieJar (c1, c2, c3)
    expect (0)(newJar.deleted.size)
    expect (3)(newJar.cookieMap.size)
    expect (c1)(newJar.cookies(0))
    expect (c2)(newJar.cookies(1))
    expect (c3)(newJar.cookies(2))
  }

  test ("parse plain cookie") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en")
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "www.w3.org", "/standards/webdesign/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
    expect (false)(value1.persistent)
  }

  test ("parse cookie with path") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Path=/standards")
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "www.w3.org", "/standards/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
  }

  test ("parse cookie with domain") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Domain=w3.org")
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "w3.org", "/standards/webdesign/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
  }

  test ("parse cookie with expiry") {
    val tomorrow = new HttpDateTimeInstant () + (24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Expires=" + tomorrow)
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "www.w3.org", "/standards/webdesign/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
    expect (tomorrow.seconds)(value1.expires.seconds)
  }

  test ("parse cookie with max age") {
    val day = 24 * 60 * 60
    val tomorrow = new HttpDateTimeInstant () + day
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Max-Age=" + day)
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "www.w3.org", "/standards/webdesign/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
    expect (tomorrow.seconds)(value1.expires.seconds)
  }

  test ("parse cookie deletion") {
    val earlier = new HttpDateTimeInstant () - 1
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Expires=" + earlier)
    val h2 = HeaderName.SET_COOKIE -> ("foo=bar")
    val key1 = CookieKey ("lang", "www.w3.org", "/standards/webdesign/")
    val key2 = CookieKey ("x", "www.w3.org", "/standards/webdesign/")
    val key3 = CookieKey ("foo", "www.w3.org", "/standards/webdesign/")
    val value = CookieValue ("en")
    val oldJar = new CookieJar (ListMap (key1 -> value), Set (key2, key3))
    val newJar = oldJar.gleanCookies (httpUrl1, Headers (h1, h2))
    expect (1)(newJar.cookieMap.size)
    expect (true)(newJar.cookieMap.contains (key3))
    expect (2)(newJar.deleted.size)
    expect (true)(newJar.deleted.contains (key1))
    expect (true)(newJar.deleted.contains (key2))
  }

  test ("parse cookie max age trumps expires") {
    val day1 = 24 * 60 * 60
    val day7 = day1 * 10
    val tomorrow = new HttpDateTimeInstant () + day1
    val nextWeek = new HttpDateTimeInstant () + day7
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Max-Age=" + day7 + "; Expires=" + tomorrow)
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "www.w3.org", "/standards/webdesign/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
    expect (nextWeek.seconds)(value1.expires.seconds)
  }

  test ("parse cookie with http only") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; HttpOnly")
    val newJar = CookieJar.empty.gleanCookies (ftpUrl1, Headers (h1))
    expect (0)(newJar.cookieMap.size)
  }

  test ("parse cookie with secure") {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Secure")
    val newJar = CookieJar.empty.gleanCookies (httpUrl1, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("lang", "www.w3.org", "/standards/webdesign/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("en")(value1.string)
    expect (true)(value1.secure)
  }

  test ("parse one realistic cookie") {
    val tenYears = new HttpDateTimeInstant () + (10 * 365 * 24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("BBC-UID=646f4472; expires=" + tenYears + "; path=/; domain=bbc.co.uk")
    val newJar = CookieJar.empty.gleanCookies (httpUrl2, Headers (h1))
    expect (1)(newJar.cookieMap.size)
    val key1 = newJar.cookieMap.keys.iterator.next ()
    expect (CookieKey ("BBC-UID", "bbc.co.uk", "/"))(key1)
    val value1 = newJar.cookieMap (key1)
    expect ("646f4472")(value1.string)
    expect (false)(value1.secure)
    expect (false)(value1.httpOnly)
    expect (false)(value1.hostOnly)
    expect (true)(value1.persistent)
    expect (tenYears.seconds)(value1.expires.seconds)
  }

  test ("parse realistic cookie list") {
    val tenYears = new HttpDateTimeInstant () + (10 * 365 * 24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("c1=v1; path=/; domain=.z.com\n" +
      "cc2=vv2; path=/; domain=.z.com; expires=Sun, 12-May-2013 11:21:33 GMT\n" +
      "ccc3=f1=5; path=/; domain=.z.com; expires=Mon, 12-Sep-2022 11:21:33 GMT")

    val newJar = CookieJar.empty.gleanCookies (httpUrl2, Headers (h1))
    expect (3)(newJar.cookieMap.size)
    val keyIterator = newJar.cookieMap.keys.iterator
    val key1 = keyIterator.next ()
    val key2 = keyIterator.next ()
    val key3 = keyIterator.next ()
    expect (CookieKey ("c1", ".z.com", "/"))(key1)
    expect (CookieKey ("cc2", ".z.com", "/"))(key2)
    expect (CookieKey ("ccc3", ".z.com", "/"))(key3)
    val value1 = newJar.cookieMap (key1)
    val value2 = newJar.cookieMap (key2)
    val value3 = newJar.cookieMap (key3)
    expect ("v1")(value1.string)
    expect ("vv2")(value2.string)
    expect ("f1=5")(value3.string)
  }

  test ("filter for request with two cookies") {
    val tenYears = new HttpDateTimeInstant () + (10 * 365 * 24 * 60 * 60)
    val cKey1 = CookieKey ("UID", "bbc.co.uk", "/")
    val cVal1 = CookieValue (string = "646f4472", expires = tenYears)

    val cKey2 = CookieKey ("LANG", "www.bbc.co.uk", "/")
    val cVal2 = CookieValue (string = "en", expires = tenYears)

    val cKey3 = CookieKey ("XYZ", "x.org", "/")
    val cVal3 = CookieValue (string = "zzz", expires = tenYears)

    val jar = new CookieJar (ListMap (cKey1 -> cVal1, cKey2 -> cVal2, cKey3 -> cVal3))

    val header = jar.filterForRequest (httpUrl2).get
    expect (HeaderName.COOKIE.name)(header.name)
    expect (true, header.value)(header.value == "UID=646f4472; LANG=en" || header.value == "LANG=en; UID=646f4472")
  }

  test ("merge") {
    val year = new HttpDateTimeInstant () + (365 * 24 * 60 * 60)

    // case 1: value to be updated
    val cKey1 = CookieKey ("k1", "x.org", "/")
    val cVal1a = CookieValue (string = "646f4472", expires = year)
    val cVal1b = CookieValue (string = "12345678", expires = year)

    // case 2: no change for differing keys
    val cKey2a = CookieKey ("k2", "x.org", "/somewhere/")
    val cVal2a = CookieValue (string = "en", expires = year)
    val cKey2b = CookieKey ("k2", "x.org", "/else/")
    val cVal2b = CookieValue (string = "fr", expires = year)

    // untouched
    val cKey3 = CookieKey ("k3", "x.org", "/")
    val cVal3 = CookieValue (string = "aaa", expires = year)

    val cKey4 = CookieKey ("k4", "x.org", "/")
    val cVal4 = CookieValue (string = "bbb", expires = year)

    val oldJar = new CookieJar (ListMap (cKey1 -> cVal1a, cKey2a -> cVal2a, cKey3 -> cVal3), Set (cKey4))
    val newJar = new CookieJar (ListMap (cKey1 -> cVal1b, cKey2b -> cVal2b, cKey4 -> cVal4))

    val merged = oldJar.merge (newJar)
    expect (5)(merged.cookieMap.size)
    expect ("12345678")(merged.cookieMap.get (cKey1).get.string)
    expect ("en")(merged.cookieMap.get (cKey2a).get.string)
    expect ("fr")(merged.cookieMap.get (cKey2b).get.string)
    expect ("aaa")(merged.cookieMap.get (cKey3).get.string)
    expect ("bbb")(merged.cookieMap.get (cKey4).get.string)
  }

  test ("add and remove") {
    val cKey1 = CookieKey ("k1", "x.org", "/")
    val cKey2 = CookieKey ("k2", "x.org", "/somewhere/")
    val cKey3 = CookieKey ("k3", "x.org", "/")
    val cKey4 = CookieKey ("k4", "x.org", "/")

    val cVal1 = CookieValue (string = "a1")
    val cVal2 = CookieValue (string = "b2")
    val cVal3 = CookieValue (string = "c3")

    val oldJar = new CookieJar (ListMap (cKey1 -> cVal1), Set(cKey2, cKey3, cKey4))
    val expandedJar1 = oldJar + (cKey2, cVal2)
    expect ("a1")(expandedJar1.cookieMap.get (cKey1).get.string)
    expect ("b2")(expandedJar1.cookieMap.get (cKey2).get.string)
    expect (false)(expandedJar1.deleted.contains(cKey2))
    expect (true)(expandedJar1.deleted.contains(cKey3))

    val expandedJar2 = expandedJar1 + Cookie(cKey3, cVal3)
    expect ("a1")(expandedJar2.cookieMap.get (cKey1).get.string)
    expect ("b2")(expandedJar2.cookieMap.get (cKey2).get.string)
    expect ("c3")(expandedJar2.cookieMap.get (cKey3).get.string)
    expect (false)(expandedJar2.deleted.contains(cKey2))
    expect (false)(expandedJar2.deleted.contains(cKey3))

    val alteredJar = expandedJar2 + Cookie(cKey3, cVal2)
    expect ("a1")(alteredJar.cookieMap.get (cKey1).get.string)
    expect ("b2")(alteredJar.cookieMap.get (cKey2).get.string)
    expect ("b2")(alteredJar.cookieMap.get (cKey3).get.string)
    expect (false)(alteredJar.deleted.contains(cKey2))
    expect (false)(alteredJar.deleted.contains(cKey3))

    val reducedJar1 = alteredJar - cKey3
    expect (false)(reducedJar1.cookieMap.contains(cKey3))
    expect (false)(reducedJar1.deleted.contains(cKey3))
    expect (true)(reducedJar1.deleted.contains(cKey4))

    val reducedJar2 = reducedJar1 - cKey4
    expect (false)(reducedJar2.deleted.contains(cKey4))
  }

  test ("url behaviour") {
    val url1 = new URL ("http://me@w3.org:8000/some/path/file.html?q=1#aaa")
    expect ("http")(url1.getProtocol)
    expect ("w3.org")(url1.getHost)
    expect (8000)(url1.getPort)
    expect (80)(url1.getDefaultPort)
    expect ("/some/path/file.html")(url1.getPath)
    expect ("/some/path/file.html?q=1")(url1.getFile)
    expect ("q=1")(url1.getQuery)
    expect ("aaa")(url1.getRef)
    expect ("me")(url1.getUserInfo)

    val url2 = new URL ("https://w3.org/")
    expect ("https")(url2.getProtocol)
    expect ("w3.org")(url2.getHost)
    expect (-1)(url2.getPort)
    expect (443)(url2.getDefaultPort)
    expect ("/")(url2.getPath)
    expect ("/")(url2.getFile)
    expect (null)(url2.getQuery)
    expect (null)(url2.getRef)
    expect (null)(url2.getUserInfo)
  }

  test ("filter cookies by name") {
    val tenYears = new HttpDateTimeInstant () + (10 * 365 * 24 * 60 * 60)
    val cKey1 = CookieKey ("X1", "bbc.co.uk", "/")
    val cVal1 = CookieValue (string = "root", expires = tenYears)

    val cKey2 = CookieKey ("X1", "bbc.co.uk", "/subdir/")
    val cVal2 = CookieValue (string = "subdir", expires = tenYears)

    val cKey3 = CookieKey ("X1", "x.org", "/")
    val cVal3 = CookieValue (string = "zzz", expires = tenYears)

    val cKey4 = CookieKey ("X2", "x.org", "/")
    val cVal4 = CookieValue (string = "aaa", expires = tenYears)

    val jar = new CookieJar (ListMap (cKey1 -> cVal1, cKey2 -> cVal2, cKey3 -> cVal3, cKey4 -> cVal4))

    val foundX1 = jar.find (_.name == "X1")
    expect (true)(foundX1.isDefined)
    expect ("root")(foundX1.get.value.string)

    val filteredX1 = jar.filter (_.name == "X1").toList
    expect (3)(filteredX1.size)
    expect ("root")(filteredX1(0).value.string)
    expect ("subdir")(filteredX1(1).value.string)
    expect ("zzz")(filteredX1(2).value.string)

    val filteredX2 = jar.filter (_.name == "X2").toList
    expect (1)(filteredX2.size)
    expect ("aaa")(filteredX2(0).value.string)

    val zipped = jar.cookies.toList
    expect (4)(zipped.size)
    expect ("root")(zipped(0).value.string)
    expect ("subdir")(zipped(1).value.string)
    expect ("zzz")(zipped(2).value.string)
    expect ("aaa")(zipped(3).value.string)
  }
}
