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

/**
 * Defines an HTTP header value consisting of a list of values.
 */
abstract class ListValue(parts: List[String]) extends Value with IndexedSeq[String] {

  def isValid = true

  override def apply(i: Int) = parts(i)

  override def length: Int = parts.length

  override def size = parts.size

  override def iterator = parts.iterator
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a comma-separated list of values.
 */
case class CommaListValue(parts: List[String]) extends ListValue(parts) {
  def toQualifiedValue = new QualifiedValue(this)

  override lazy val value = parts.mkString(", ")
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a semicolon-separated list of values.
 */
case class SemicolonListValue(parts: List[String]) extends ListValue(parts) {
  def toQualifiers = Qualifiers(this)

  override lazy val value = parts.mkString("; ")
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Constructs CommaListValues from strings.
 */
object CommaListValue {

  /** Constructs an instance by splitting at the commas. */
  def split(value: String) = new CommaListValue(HttpUtil.split(value, ',') map (_.trim) toList)
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Constructs SemicolonListValues from strings.
 */
object SemicolonListValue {

  /** Constructs an instance by splitting at the semicolons. */
  def split(value: String) = new SemicolonListValue(HttpUtil.split(value, ';') map (_.trim) toList)
}
