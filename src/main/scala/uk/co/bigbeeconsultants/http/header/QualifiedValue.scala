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

import uk.co.bigbeeconsultants.http.Util

/**
 * Specifies a key/value pair used as one of (potentially many) parameters attached to a compound header value.
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


case class Part(value: String, qualifier: List[Qualifier] = Nil) {
  override def toString =
    if (qualifier.isEmpty) value
    else value + ";" + qualifier.mkString(";")
}


/**
 * Defines an HTTP header with a list of qualifiers. Typically, such values are
 * used for the 'accept' category of headers.
 */
case class QualifiedValue(value: String) {

  val parts: List[Part] = {
    val parts = for (v <- Util.split(value, ',')) yield {
      val t = Util.split(v.trim, ';')
      val qualifiers = for (q <- t.tail) yield {
        Qualifier(q.trim)
      }
      Part(t.head, qualifiers.toList)
    }
    parts.toList
  }

  override val toString = parts.mkString(", ")
}
