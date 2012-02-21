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
import uk.co.bigbeeconsultants.lhc.response.{Status, Response, StringBody}
import uk.co.bigbeeconsultants.lhc.HttpDateTimeInstant

class CookieJarTest {

  val ftpUrl1 = Request.get(new URL("ftp://www.w3.org/standards/webdesign/htmlcss"))
  val httpUrl1 = Request.get(new URL("http://www.w3.org/standards/webdesign/htmlcss"))
  val httpUrl2 = Request.get(new URL("http://www.bbc.co.uk/radio/stations/radio1"))
  val httpsUrl1 = Request.get(new URL("https://www.w3.org/login/"))
  val body = new StringBody(MediaType.TEXT_PLAIN, "")
  val ok = Status(200, "OK")

  @Test
  def noCookies() {
    val response = Response(httpUrl1, ok, body, Headers(List()))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(0, newJar.cookies.size)
  }

  @Test
  def parsePlainCookie() {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en")
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "www.w3.org", "/standards/webdesign/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
    assertFalse(value1.persistent)
  }

  @Test
  def parseCookieWithPath() {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Path=/standards")
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "www.w3.org", "/standards/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
  }

  @Test
  def parseCookieWithDomain() {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Domain=w3.org")
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "w3.org", "/standards/webdesign/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
  }

  @Test
  def parseCookieWithExpiry() {
    val tomorrow = new HttpDateTimeInstant() + (24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Expires=" + tomorrow)
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "www.w3.org", "/standards/webdesign/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
    assertEquals(tomorrow.seconds, value1.expires.seconds)
  }

  @Test
  def parseCookieWithMaxAge() {
    val day = 24 * 60 * 60
    val tomorrow = new HttpDateTimeInstant() + day
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Max-Age=" + day)
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "www.w3.org", "/standards/webdesign/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
    assertEquals(tomorrow.seconds, value1.expires.seconds)
  }

  @Test
  def parseCookieDeletion() {
    val earlier = new HttpDateTimeInstant() - 1
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Expires=" + earlier)
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val key = CookieKey("lang", "www.w3.org", "/standards/webdesign/")
    val value = CookieValue("en")
    val oldJar = new CookieJar(Map(key -> value))
    val newJar = oldJar.updateCookies(response)
    assertEquals(0, newJar.cookies.size)
  }

  @Test
  def parseCookieMaxAgeTrumpsExpires() {
    val day1 = 24 * 60 * 60
    val day7 = day1 * 10
    val tomorrow = new HttpDateTimeInstant() + day1
    val nextWeek = new HttpDateTimeInstant() + day7
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Max-Age=" + day7 + "; Expires=" + tomorrow)
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "www.w3.org", "/standards/webdesign/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
    assertEquals(nextWeek.seconds, value1.expires.seconds)
  }

  @Test
  def parseCookieWithHttpOnly() {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; HttpOnly")
    val response = Response(ftpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(0, newJar.cookies.size)
  }

  @Test
  def parseCookieWithSecure() {
    val h1 = HeaderName.SET_COOKIE -> ("lang=en; Secure")
    val response = Response(httpUrl1, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("lang", "www.w3.org", "/standards/webdesign/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("en", value1.value)
    assertTrue(value1.secure)
  }

  @Test
  def parseRealisticCookie() {
    val tenYears = new HttpDateTimeInstant() + (10 * 365 * 24 * 60 * 60)
    val h1 = HeaderName.SET_COOKIE -> ("BBC-UID=646f4472; expires=" + tenYears + "; path=/; domain=bbc.co.uk")
    val response = Response(httpUrl2, ok, body, Headers(List(h1)))
    val newJar = CookieJar.updateCookies(response)
    assertEquals(1, newJar.cookies.size)
    val key1 = newJar.cookies.keys.iterator.next()
    assertEquals(CookieKey("BBC-UID", "bbc.co.uk", "/"), key1)
    val value1 = newJar.cookies(key1)
    assertEquals("646f4472", value1.value)
    assertFalse(value1.secure)
    assertFalse(value1.httpOnly)
    assertFalse(value1.hostOnly)
    assertTrue(value1.persistent)
    assertEquals(tenYears.seconds, value1.expires.seconds)
  }

  @Test
  def filterForRequest_withTwoCookies() {
    val tenYears = new HttpDateTimeInstant() + (10 * 365 * 24 * 60 * 60)
    val cKey1 = CookieKey("UID", "bbc.co.uk", "/")
    val cVal1 = CookieValue(value = "646f4472", expires = tenYears)

    val cKey2 = CookieKey("LANG", "www.bbc.co.uk", "/")
    val cVal2 = CookieValue(value = "en", expires = tenYears)

    val cKey3 = CookieKey("XYZ", "x.org", "/")
    val cVal3 = CookieValue(value = "zzz", expires = tenYears)

    val jar = new CookieJar(Map(cKey1 -> cVal1, cKey2 -> cVal2, cKey3 -> cVal3))

    val header = jar.filterForRequest(httpUrl2.url)
    assertEquals(HeaderName.COOKIE.name, header.name)
    assertTrue(header.value, header.value == "UID=646f4472; LANG=en" ||
      header.value == "LANG=en; UID=646f4472")
  }
}
