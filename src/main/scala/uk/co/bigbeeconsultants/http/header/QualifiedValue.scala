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
 * Specifies a key/value pair used as one of (potentially many) parameters attached to a compound header value.
 * These are used within [[uk.co.bigbeeconsultants.http.header.QualifiedPart]].
 */
case class Qualifier(label: String, value: String) {
  override def toString =
    if (value.length > 0) label + "=" + value
    else label

  def isValid = !label.isEmpty
}

object Qualifier {
  def apply(str: String) = {
    val t = HttpUtil.divide (str, '=')
    new Qualifier (t._1, t._2)
  }
}


/**
 * A qualified part is one list element in a [[uk.co.bigbeeconsultants.http.header.QualifiedValue]], the list
 * being delimited by commas. Besides having a value, it may itself have a semi-colon delimited list of
 * [[uk.co.bigbeeconsultants.http.header.Qualifier]]s.
 */
case class QualifiedPart(value: String, qualifier: List[Qualifier] = Nil) {
  override def toString =
    if (qualifier.isEmpty) value
    else value + ";" + qualifier.mkString (";")

  def isValid = !value.isEmpty && value.indexOf('=') < 0 && qualifier.forall(_.isValid)
}

object QualifiedPart {
  def parse(str: String) = {
    val t = HttpUtil.split (str.trim, ';')
    val qualifiers = t.tail.map {
      q => Qualifier (q.trim)
    }
    new QualifiedPart (t.head, qualifiers.toList)
  }
}


/**
 * Defines an HTTP header with a list of qualifiers. Typically, such values are used for the 'accept' category of
 * headers. These are some of the most complex of HTTP headers, consisting of a comma-separated list of
 * [[uk.co.bigbeeconsultants.http.header.QualifiedPart]]s.
 * Each qualified part is a value and a list of qualifiers. Each qualifier is a key=value pair.
 * Example value:
 {{{
 text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5
 }}}
 */
case class QualifiedValue(value: String) extends Value {

  val parts: List[QualifiedPart] = {
    HttpUtil.split (value, ',').map {
      v: String => QualifiedPart.parse (v)
    }.toList
  }

  lazy val isValid = parts.forall(_.isValid)

  def apply(i: Int) = parts (i).value

  override val toString = parts.mkString (", ")
}
