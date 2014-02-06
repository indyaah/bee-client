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

/**
 * Specifies a key/value pair used as one of (potentially many) parameters attached to a compound header value.
 * These are used within [[uk.co.bigbeeconsultants.http.header.Qualifiers]].
 */
case class NameVal(name: String, value: Option[String]) {
  override def toString =
    if (value.isDefined) name + "=" + value.get
    else name

  def isValid = !name.isEmpty

  def valueWithoutQuotes: Option[String] = {
    if (value.isEmpty) None
    else Some(unquote(value.get))
  }
}

//---------------------------------------------------------------------------------------------------------------------

object NameVal {
  def apply(str: String) = {
    val s = str.indexOf('=')
    if (s >= 0 && s < str.length) {
      val a = str.substring(0, s)
      val b = str.substring(s + 1)
      new NameVal(a, Some(b))
    }
    else new NameVal(str, None)
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Qualifiers consists of a semi-colon delimited list of [[uk.co.bigbeeconsultants.http.header.NameVal]]s.
 * Often, the first of these is a label without a value.
 * Qualifiers is itself one list element in a [[uk.co.bigbeeconsultants.http.header.QualifiedValue]].
 */
case class Qualifiers(qualifiers: List[NameVal] = Nil) extends Iterable[NameVal] {

  def isValid = qualifiers.forall(_.isValid)

  def value = qualifiers(0).name

  def apply(i: Int) = qualifiers.apply(i)

  override def size = qualifiers.size

  override def iterator = qualifiers.iterator

  override lazy val toString = qualifiers.mkString(";")
}

//---------------------------------------------------------------------------------------------------------------------

object Qualifiers {
  def apply(str: String): Qualifiers = apply(SemicolonListValue.split(str))

  def apply(list: SemicolonListValue): Qualifiers = {
    val qualifiers = list.parts.map {
      q => NameVal(q.trim)
    }
    new Qualifiers(qualifiers.toList)
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header with a list of qualifiers. These are derived from a CommaListValue and are typically
 * used for the 'accept' category of headers. These are some of the most complex of HTTP headers, consisting of
 * a comma-separated list of [[uk.co.bigbeeconsultants.http.header.Qualifiers]]s.
 * Each qualified part is a value and a list of qualifiers. Each qualifier is a key=value pair.
 *
 * Example value:
 {{{
 text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5
 }}}
 *
 * The API emulates a two-dimensional array. The first index gives
 * [[uk.co.bigbeeconsultants.http.header.Qualifiers]]s and the second
 * [[uk.co.bigbeeconsultants.http.header.NameVal]]s.
 */
case class QualifiedValue(list: CommaListValue) extends Iterable[Qualifiers] with Value {

  def this(value: String) = this(CommaListValue.split(value))

  val parts: List[Qualifiers] = list.map(Qualifiers(_)).toList

  lazy val isValid = parts.forall(_.isValid)

  def apply(i: Int) = parts(i)

  override def size = parts.size

  override def iterator = parts.iterator

  lazy val value = parts.mkString(", ")
}

//---------------------------------------------------------------------------------------------------------------------

object QualifiedValue {
  def apply(str: String) = new QualifiedValue(CommaListValue.split(str))

  def ifValid(value: String) = {
    val v = apply(value)
    if (v.isValid) Some(v) else None
  }
}