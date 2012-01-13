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

  def toInt: Int = {
    assume(values.size == 1)
    value.toInt
  }

  def toLong: Long = {
    assume(values.size == 1)
    value.toLong
  }

  def getHeaderFieldDate(name: String, defaultValue: Long = 0): Long = {
    assume(values.size == 1)
    val dateString = value
    try {
      if (dateString.indexOf("GMT") == -1) {
        Date.parse(dateString + " GMT")
      } else {
        Date.parse(dateString)
      }
    }
    catch {
      case e: Exception => {
      }
    }
    defaultValue
  }

  override def toString = name + ": " + values.mkString(", ")
}

object Header {
  val ACCEPT = "Accept"
  val ACCEPT_CHARSET = "Accept-Charset"
  val ACCEPT_ENCODING = "Accept-Encoding"
  val ACCEPT_LANGUAGE = "Accept-Language"
  val ACCEPT_RANGES = "Accept-Ranges"
  val AGE = "Age"
  val ALLOW = "Allow"
  val AUTHORIZATION = "Authorization"
  val CACHE_CONTROL = "Cache-Control"
  val CONNECTION = "Connection"
  val CONTENT_ENCODING = "Content-Encoding"
  val CONTENT_LANGUAGE = "Content-Language"
  val CONTENT_LENGTH = "Content-Length"
  val CONTENT_LOCATION = "Content-Location"
  val CONTENT_MD5 = "Content-MD5"
  val CONTENT_RANGE = "Content-Range"
  val CONTENT_TYPE = "Content-Type"
  val COOKIE = "Cookie"
  val DATE = "Date"
  val ETAG = "ETag"
  val EXPECT = "Expect"
  val EXPIRES = "Expires"
  val FROM = "From"
  val HOST = "Host"
  val IF_MATCH = "If-Match"
  val IF_MODIFIED_SINCE = "If-Modified-Since"
  val IF_NONE_MATCH = "If-None-Match"
  val IF_RANGE = "If-Range"
  val IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
  val LAST_MODIFIED = "Last-Modified"
  val LOCATION = "Location"
  val MAX_FORWARDS = "Max-Forwards"
  val PRAGMA = "Pragma"
  val PROXY_AUTHENTICATE = "Proxy-Authenticate"
  val PROXY_AUTHORIZATION = "Proxy-Authorization"
  val RANGE = "Range"
  val REFERER = "Referer"
  val RETRY_AFTER = "Retry-After"
  val SERVER = "Server"
  val TE = "TE"
  val TRAILER = "Trailer"
  val TRANSFER_ENCODING = "Transfer-Encoding"
  val UPGRADE = "Upgrade"
  val USER_AGENT = "User-Agent"
  val VARY = "Vary"
  val VIA = "Via"
  val WARNING = "Warning"
  val WWW_AUTHENTICATE = "WWW-Authenticate"


  def apply(raw: String): Header = {
    val t = Util.divide(raw, ':')
    apply(t._1.trim, t._2.trim)
  }

  def apply(name: String, rawValue: String): Header = {
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
