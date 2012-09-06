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

import uk.co.bigbeeconsultants.http.util.HttpUtil
import uk.co.bigbeeconsultants.http.HttpDateTimeInstant

/**
 * Provides an HTTP header. Bear in mind that header names are case-insensitive, but you are advised to stick to
 * the canonical capitalisation, which is as given by the HeaderName object values.
 */
case class Header(name: String, value: String) {

  /** Converts the value to an integer. */
  def toInt: Int = value.toInt

  /** Converts the value to a long. */
  def toLong: Long = value.toLong

  /** Converts the value to an HttpDateTimeInstant. */
  def toDate(defaultValue: HttpDateTimeInstant = HttpDateTimeInstant.zero): HttpDateTimeInstant = HttpDateTimeInstant.parse (value, defaultValue)

  /** Converts the value to a qualified value. */
  def toQualifiedValue = QualifiedValue (value)

  /** Converts the value to a range value. */
  def toRangeValue = RangeValue (value)

  /** Converts the value to a media type. */
  def toMediaType = MediaType (value)

  override def toString = name + ": " + value

  lazy val hasListValue = HeaderName.headersWithListValues.contains (name)
}


object Header {
  @deprecated
  def apply(tuple: (String, String)): Header = new Header(tuple._1, tuple._2)

  /**
   * Constructs a header by splitting a raw string at the first ':'.
   */
  def apply(raw: String): Header = {
    val t = HttpUtil.divide (raw, ':')
    apply (t._1.trim, t._2.trim)
  }
}
