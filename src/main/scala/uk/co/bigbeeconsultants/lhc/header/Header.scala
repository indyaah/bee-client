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

import java.util.Date
import uk.co.bigbeeconsultants.lhc.Util

case class HeaderName(name: String) {
  def -> (newValue: String) = new Header(name, newValue)
}

object HeaderName {
  implicit def headerNameToString(hn: HeaderName) = hn.name
}

/**
 * Provides an HTTP header.
 */
case class Header(name: String, value: String) {

  def toInt: Int = value.toInt

  def toLong: Long = value.toLong

  def toDate(defaultValue: Date = Util.defaultDate): Date = Util.parseHttpDate(value)

  def toQualifiedValue = QualifiedValue(value)

  def toMediaType = MediaType(value)

  def toCookie = Cookie(value)

  override def toString = name + ": " + value

  lazy val hasListValue = Header.headersWithListValues.contains(name)
}


object Header {
  // General headers
  val CACHE_CONTROL = HeaderName("Cache-Control")
  val CONNECTION = HeaderName("Connection")
  val DATE = HeaderName("Date")
  val PRAGMA = HeaderName("Pragma")
  val TRAILER = HeaderName("Trailer")
  val TRANSFER_ENCODING = HeaderName("Transfer-Encoding")
  val UPGRADE = HeaderName("Upgrade")
  val VIA = HeaderName("Via")
  val WARNING = HeaderName("Warning")

  // Request & response headers
  val ACCEPT_RANGES = HeaderName("Accept-Ranges")

  // Request headers
  val ACCEPT = HeaderName("Accept")
  val ACCEPT_CHARSET = HeaderName("Accept-Charset")
  val ACCEPT_ENCODING = HeaderName("Accept-Encoding")
  val ACCEPT_LANGUAGE = HeaderName("Accept-Language")
  val AUTHORIZATION = HeaderName("Authorization")
  val COOKIE = HeaderName("Cookie")
  val EXPECT = HeaderName("Expect")
  val FROM = HeaderName("From")
  val HOST = HeaderName("Host")
  val IF_MATCH = HeaderName("If-Match")
  val IF_MODIFIED_SINCE = HeaderName("If-Modified-Since")
  val IF_NONE_MATCH = HeaderName("If-None-Match")
  val IF_RANGE = HeaderName("If-Range")
  val IF_UNMODIFIED_SINCE = HeaderName("If-Unmodified-Since")
  val MAX_FORWARDS = HeaderName("Max-Forwards")
  val PROXY_AUTHORIZATION = HeaderName("Proxy-Authorization")
  val RANGE = HeaderName("Range")
  val REFERER = HeaderName("Referer")
  val TE = HeaderName("TE")
  val USER_AGENT = HeaderName("User-Agent")

  // Response headers
  val AGE = HeaderName("Age")
  val ETAG = HeaderName("ETag")
  val LOCATION = HeaderName("Location")
  val PROXY_AUTHENTICATE = HeaderName("Proxy-Authenticate")
  val RETRY_AFTER = HeaderName("Retry-After")
  val SET_COOKIE = HeaderName("Set-Cookie")
  val SET_COOKIE2 = HeaderName("Set-Cookie2")
  val SERVER = HeaderName("Server")
  val VARY = HeaderName("Vary")
  val WWW_AUTHENTICATE = HeaderName("WWW-Authenticate")

  // Entity headers
  val ALLOW = HeaderName("Allow")
  val CONTENT_ENCODING = HeaderName("Content-Encoding")
  val CONTENT_LANGUAGE = HeaderName("Content-Language")
  val CONTENT_LENGTH = HeaderName("Content-Length")
  val CONTENT_LOCATION = HeaderName("Content-Location")
  val CONTENT_MD5 = HeaderName("Content-MD5")
  val CONTENT_RANGE = HeaderName("Content-Range")
  val CONTENT_TYPE = HeaderName("Content-Type")
  val EXPIRES = HeaderName("Expires")
  val LAST_MODIFIED = HeaderName("Last-Modified")


  val headersWithListValues: Set[String] = Set(ACCEPT.name,
    ACCEPT_CHARSET.name,
    ACCEPT_ENCODING.name,
    ACCEPT_LANGUAGE.name,
    CACHE_CONTROL.name,
    CONTENT_LANGUAGE.name,
    CONTENT_TYPE.name,
    EXPECT.name,
    PRAGMA.name,
    RANGE.name,
    TE.name,
    UPGRADE.name,
    VIA.name)

  def apply(raw: String): Header = {
    val t = Util.divide(raw, ':')
    apply(t._1.trim, t._2.trim)
  }
}
