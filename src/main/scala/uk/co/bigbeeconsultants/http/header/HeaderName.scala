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

import reflect.Type

/**
 * Constructs a new HeaderName instance from a string. Bear in mind that header names are case-insensitive,
 * but you are advised to stick to the canonical capitalisation, which is as given by the HeaderName object values.
 */
case class HeaderName(name: String) {
  /** Constructs a new header from this header name and a specified value. */
  def ->(newValue: String) = new Header (name, newValue)

  /** Constructs a new header from this header name and a specified value. */
  def →(newValue: String) = new Header (name, newValue)

  override def toString = name
}

/** General header names; also those used for both requests and responses. */
sealed trait GeneralHeaderName {
  val ACCEPT_RANGES = HeaderName ("Accept-Ranges")
  val CACHE_CONTROL = HeaderName ("Cache-Control")
  val DATE = HeaderName ("Date") // HttpDateTimeInstant
  val PRAGMA = HeaderName ("Pragma") // ListValue
  val UPGRADE = HeaderName ("Upgrade") // ListValue
  val VIA = HeaderName ("Via") // ListValue
  val WARNING = HeaderName ("Warning") // String value (in ISO8859-1)
}

/** Header names used only for requests. */
sealed trait RequestHeaderName {
  val ACCEPT = HeaderName ("Accept") // QualifiedValue
  val ACCEPT_CHARSET = HeaderName ("Accept-Charset") // QualifiedValue (case-insensitive)
  val ACCEPT_ENCODING = HeaderName ("Accept-Encoding") // QualifiedValue ("gzip", "compress", "deflate" etc)
  val ACCEPT_LANGUAGE = HeaderName ("Accept-Language") // QualifiedValue
  val AUTHORIZATION = HeaderName ("Authorization")
  val COOKIE = HeaderName ("Cookie")
  val OBSOLETE_COOKIE2 = HeaderName ("Cookie2")
  val DO_NOT_TRACK = HeaderName ("DNT") // Int
  val EXPECT = HeaderName ("Expect")
  val FROM = HeaderName ("From") // String
  val HOST = HeaderName ("Host") // String
  val IF_MATCH = HeaderName ("If-Match") // ListValue
  val IF_MODIFIED_SINCE = HeaderName ("If-Modified-Since") // HttpDateTimeInstant
  val IF_NONE_MATCH = HeaderName ("If-None-Match") // ListValue
  val IF_RANGE = HeaderName ("If-Range")
  val IF_UNMODIFIED_SINCE = HeaderName ("If-Unmodified-Since") // HttpDateTimeInstant
  val MAX_FORWARDS = HeaderName ("Max-Forwards") // Int
  val PROXY_AUTHORIZATION = HeaderName ("Proxy-Authorization")
  val RANGE = HeaderName ("Range") // RangePart
  val REFERER = HeaderName ("Referer") // String
  val USER_AGENT = HeaderName ("User-Agent") // String
}

/** Header names used only for responses. */
sealed trait ResponseHeaderName {
  val AGE = HeaderName ("Age") // Int
  val ETAG = HeaderName ("ETag") // String
  val LOCATION = HeaderName ("Location") // String
  val PROXY_AUTHENTICATE = HeaderName ("Proxy-Authenticate")
  val RETRY_AFTER = HeaderName ("Retry-After") // HttpDateTimeInstant or Int
  val SET_COOKIE = HeaderName ("Set-Cookie")
  val OBSOLETE_SET_COOKIE2 = HeaderName ("Set-Cookie2")
  val SERVER = HeaderName ("Server") // String
  val VARY = HeaderName ("Vary") // ListValue
  val WWW_AUTHENTICATE = HeaderName ("WWW-Authenticate") // ListValue
}

/** Header names used only for entity metadata. */
sealed trait EntityHeaderName {
  val ALLOW = HeaderName ("Allow") // ListValue
  val CONTENT_ENCODING = HeaderName ("Content-Encoding") // ListValue
  val CONTENT_LANGUAGE = HeaderName ("Content-Language") // ListValue
  val CONTENT_LENGTH = HeaderName ("Content-Length") // Int
  val CONTENT_LOCATION = HeaderName ("Content-Location") // String
  val CONTENT_MD5 = HeaderName ("Content-MD5")
  val CONTENT_RANGE = HeaderName ("Content-Range") // RangeValue
  val CONTENT_TYPE = HeaderName ("Content-Type") // MediaType
  val EXPIRES = HeaderName ("Expires") // HttpDateTimeInstant
  val LAST_MODIFIED = HeaderName ("Last-Modified") // HttpDateTimeInstant
}

/**
 * Provides the header names in RFC2616 (inter alia) using the canonical capitalisation. These values are
 * type-safe and therefore a better choice than arbitrarily using strings to create headers.
 */
object HeaderName extends GeneralHeaderName with RequestHeaderName with ResponseHeaderName with EntityHeaderName {
  implicit def headerNameToString(hn: HeaderName) = hn.name

  val headersWithListValues: Set[String] = Set (ACCEPT.name,
    ACCEPT_CHARSET.name,
    ACCEPT_ENCODING.name,
    ACCEPT_LANGUAGE.name,
    CACHE_CONTROL.name,
    CONTENT_LANGUAGE.name,
    CONTENT_TYPE.name,
    EXPECT.name,
    PRAGMA.name,
    RANGE.name,
    UPGRADE.name,
    VIA.name)
}

/**
 * These headers are used internally by HttpURLConnection and should not normally appear in client applications.
 * They are for expert use only.
 */
object ExpertHeaderName {
  val CONNECTION = HeaderName ("Connection") // ListValue
  val TE = HeaderName ("TE") // QualifiedValue
  val TRAILER = HeaderName ("Trailer") // ListValue
  val TRANSFER_ENCODING = HeaderName ("Transfer-Encoding") // ListValue
}