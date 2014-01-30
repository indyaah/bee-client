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

import uk.co.bigbeeconsultants.http.util.HttpUtil._

trait Value {
  def value: String

  def isValid: Boolean

  override def toString = value
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides an HTTP header. Bear in mind that header names are case-insensitive, but you are advised to stick to
 * the canonical capitalisation, which is as given by the HeaderName object values.
 */
case class Header(name: String, value: String) {

  /**
   * Converts the value to a qualified value.
   *
   * Accept | Accept-Charset | Accept-Encoding | Accept-Language
   */
  def toQualifiedValue = toListValue.toQualifiedValue

  /**
   * Converts the value to an integer number.
   *
   * Age | Content-Length | Max-Forwards
   */
  def toNumber = NumberValue(value)

  /**
   * Converts the value to an HttpDateTimeInstant.
   *
   * Date | Expires | If-Modified-Since | If-Unmodified-Since | Last-Modified
   */
  def toDate: DateValue = DateValue(value)

  /**
   * Converts the value to a comma-separated list value.
   *
   * Accept-Ranges | Allow | Cache-Control | Connection | Content-Encoding | Content-Language
   */
  def toListValue = CommaListValue.split(value)

  /**
   * Converts the value to an authenticate value.
   *
   * WWW-Authenticate
   */
  def toAuthenticateValue = AuthenticateValue(value)

  /**
   * Converts the value to a range value.
   *
   * Range
   */
  def toRangeValue = RangeValue(value)

  /**
   * Converts the value to a media type.
   *
   * Content-Type
   */
  def toMediaType = MediaType(value)

  /**
   * Converts the value to a single entity tag.
   *
   * Etag
   */
  def toEntityTag = EntityTag(value)

  /**
   * Converts the value to a list of entity tags.
   *
   * If-Match | If-None-Match
   */
  def toEntityTagList = EntityTagListValue(value)

  /**
   * Converts the value to a warning value.
   *
   * Warning
   */
  def toWarning = WarningValue(value)

  /**
   * Converts the value to a cache-control value.
   *
   * Cache-Control
   */
  def toCacheControlValue = CacheControlValue(value)

  /** Header name equalsIgnoreCase other name. */
  def =~=(other: String) = name equalsIgnoreCase other

  /** Header name equalsIgnoreCase other name. */
  def =~=(other: HeaderName) = name equalsIgnoreCase other.name

  /** Header name equalsIgnoreCase other name. */
  def =~=(other: Header) = name equalsIgnoreCase other.name

  override def toString = name + ": " + value

  lazy val hasListValue = HeaderName.headersWithListValues.contains(name)
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides a factory constructor for headers based on the strings typical in HTTP traffic.
 */
object Header {
  /**
   * Constructs a header by splitting a raw string at the first ':'.
   */
  def apply(raw: String): Header = {
    val t = divide(raw, ':')
    apply(t._1.trim, t._2.trim)
  }
}
