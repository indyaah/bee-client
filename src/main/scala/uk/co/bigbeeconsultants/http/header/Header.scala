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

trait Value {
  def isValid: Boolean
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides an HTTP header. Bear in mind that header names are case-insensitive, but you are advised to stick to
 * the canonical capitalisation, which is as given by the HeaderName object values.
 */
case class Header(name: String, value: String) {

  /** Converts the value to an integer number. */
  def toNumber = NumberValue (value)

  /** Converts the value to an HttpDateTimeInstant. */
  def toDate: DateValue = DateValue (value)

  /** Converts the value to a qualified value. */
  def toQualifiedValue = QualifiedValue (value)

  /** Converts the value to an authenticate value. */
  def toAuthenticateValue = AuthenticateValue (value)

  /** Converts the value to a range value. */
  def toRangeValue = RangeValue (value)

  /** Converts the value to a media type. */
  def toMediaType = MediaType (value)

  /** Header name equalsIgnoreCase other name. */
  def =~=(other: String) = name equalsIgnoreCase other

  /** Header name equalsIgnoreCase other name. */
  def =~=(other: HeaderName) = name equalsIgnoreCase other.name

  /** Header name equalsIgnoreCase other name. */
  def =~=(other: Header) = name equalsIgnoreCase other.name

  override def toString = name + ": " + value

  lazy val hasListValue = HeaderName.headersWithListValues.contains (name)
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
    val t = HttpUtil.divide (raw, ':')
    apply (t._1.trim, t._2.trim)
  }
}
