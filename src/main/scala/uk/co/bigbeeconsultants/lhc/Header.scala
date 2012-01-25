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

package uk.co.bigbeeconsultants.lhc

import java.util.Date

/**
 * Specifies a key/value pair used as one of (potentially many) parameters attached to a header value.
 */
case class Qualifier(label: String, value: String) {
  override def toString =
    if (value.length() > 0) label + "=" + value
    else label
}

object Qualifier {
  def apply(str: String) = {
    val t = Util.divide(str, '=')
    new Qualifier(t._1, t._2)
  }
}


/**
 * Provides an interface for values within headers.
 */
trait Valuable {
  def value: String

  def qualifier: List[Qualifier]
}

case class Value(value: String, qualifier: List[Qualifier] = Nil) extends Valuable {
  override def toString =
    if (qualifier.isEmpty) value
    else value + ";" + qualifier.mkString(";")
}


/**
 * Provides the HTTP header itself.
 */
case class Header(name: String, values: List[Valuable]) {

  def value = values.mkString(", ")

  def value0 = {
    assume(values.size == 1)
    values(0).value
  }

  def toInt: Int = {
    assume(values.size == 1)
    value0.toInt
  }

  def toLong: Long = {
    assume(values.size == 1)
    value0.toLong
  }

  def toDate(defaultValue: Date = Util.defaultDate): Date = {
    assume(values.size == 1)
    Util.parseHttpDate(value0)
  }

  override def toString = name + ": " + value
}

object Header {
  // General headers
  val CACHE_CONTROL = "Cache-Control"
  val CONNECTION = "Connection"
  val DATE = "Date"
  val PRAGMA = "Pragma"
  val TRAILER = "Trailer"
  val TRANSFER_ENCODING = "Transfer-Encoding"
  val UPGRADE = "Upgrade"
  val VIA = "Via"
  val WARNING = "Warning"

  // Request & response headers
  val ACCEPT_RANGES = "Accept-Ranges"

  // Request headers
  val ACCEPT = "Accept"
  val ACCEPT_CHARSET = "Accept-Charset"
  val ACCEPT_ENCODING = "Accept-Encoding"
  val ACCEPT_LANGUAGE = "Accept-Language"
  val AUTHORIZATION = "Authorization"
  val COOKIE = "Cookie"
  val EXPECT = "Expect"
  val FROM = "From"
  val HOST = "Host"
  val IF_MATCH = "If-Match"
  val IF_MODIFIED_SINCE = "If-Modified-Since"
  val IF_NONE_MATCH = "If-None-Match"
  val IF_RANGE = "If-Range"
  val IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
  val MAX_FORWARDS = "Max-Forwards"
  val PROXY_AUTHORIZATION = "Proxy-Authorization"
  val RANGE = "Range"
  val REFERER = "Referer"
  val TE = "TE"
  val USER_AGENT = "User-Agent"

  // Response headers
  val AGE = "Age"
  val ETAG = "ETag"
  val LOCATION = "Location"
  val PROXY_AUTHENTICATE = "Proxy-Authenticate"
  val RETRY_AFTER = "Retry-After"
  val SET_COOKIE = "Set-Cookie"
  val SET_COOKIE2 = "Set-Cookie2"
  val SERVER = "Server"
  val VARY = "Vary"
  val WWW_AUTHENTICATE = "WWW-Authenticate"

  // Entity headers
  val ALLOW = "Allow"
  val CONTENT_ENCODING = "Content-Encoding"
  val CONTENT_LANGUAGE = "Content-Language"
  val CONTENT_LENGTH = "Content-Length"
  val CONTENT_LOCATION = "Content-Location"
  val CONTENT_MD5 = "Content-MD5"
  val CONTENT_RANGE = "Content-Range"
  val CONTENT_TYPE = "Content-Type"
  val EXPIRES = "Expires"
  val LAST_MODIFIED = "Last-Modified"


  val headersWithListValues = Set(ACCEPT, ACCEPT_CHARSET, ACCEPT_ENCODING, ACCEPT_LANGUAGE,
    CACHE_CONTROL, CONTENT_LANGUAGE, CONTENT_TYPE, EXPECT, PRAGMA, RANGE, TE, UPGRADE, VIA)

  def apply(raw: String): Header = {
    val t = Util.divide(raw, ':')
    apply(t._1.trim, t._2.trim)
  }

  def apply(name: String, rawValue: String): Header = {
    if (!headersWithListValues.contains(name)) {
      Header(name, List(Value(rawValue)))
    }
    else {
      val values = for (v <- rawValue.split(',')) yield {
        val t = v.trim.split(';')
        val qualifiers = for (q <- t.tail) yield {
          Qualifier(q.trim)
        }
        Value(t.head, qualifiers.toList)
      }
      Header(name, values.toList)
    }
  }
}
