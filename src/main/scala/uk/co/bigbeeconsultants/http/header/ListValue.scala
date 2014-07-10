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
abstract class ListValue[T](parts: List[T], separator: String) extends IndexedSeq[T] with Value {

  final override def apply(i: Int) = parts(i)

  final override def length: Int = parts.length

  final override def size = parts.size

  final override def iterator = parts.iterator

  override lazy val value = parts.mkString(separator)
}

//---------------------------------------------------------------------------------------------------------------------


trait IgnorableCaseValue[T] {
  def toLowerCase: ListValue[T]
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a comma-separated list of values.
 */
case class CommaListValue(parts: List[String]) extends ListValue(parts, ", ") with IgnorableCaseValue[String] {
  def toQualifiedValue = new QualifiedValue(this)
  val isValid = true

  override def toLowerCase = new CommaListValue(parts.map(_.toLowerCase))
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Constructs CommaListValues from strings.
 */
object CommaListValue {

  /** Constructs an instance by splitting at the commas. */
  def split(value: String) = new CommaListValue(HttpUtil.splitQuoted(value, ',') map (_.trim))
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a semicolon-separated list of values.
 */
case class SemicolonListValue(parts: List[String]) extends ListValue(parts,"; ") with IgnorableCaseValue[String] {
  def toQualifiers = Qualifiers(this)
  val isValid = true

  override def toLowerCase = new SemicolonListValue(parts.map(_.toLowerCase))
}
//---------------------------------------------------------------------------------------------------------------------

/**
 * Constructs SemicolonListValues from strings.
 */
object SemicolonListValue {

  /** Constructs an instance by splitting at the semicolons. */
  def split(value: String) = new SemicolonListValue(HttpUtil.splitQuoted(value, ';') map (_.trim))
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a list of values, each of which is a name and optional value.
 */
case class NameValListValue(parts: List[NameVal], separator: String) extends ListValue(parts, separator) with IgnorableCaseValue[NameVal] {
  def isValid = parts.forall(_.isValid)

  override def toLowerCase = new NameValListValue(parts.map(_.toLowerCase), separator)
}

//---------------------------------------------------------------------------------------------------------------------

object NameValListValue {
  /** Constructs an instance by splitting at the commas. */
  def split(value: String) = {
    new NameValListValue(HttpUtil.splitQuoted(value, ',') map (s => NameVal(s.trim)), ", ")
  }
}
