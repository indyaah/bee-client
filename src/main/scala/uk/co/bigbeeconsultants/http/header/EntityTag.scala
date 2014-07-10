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
import scala.collection.mutable.ArrayBuffer

case class EntityTag(opaqueTag: String, isWeak: Boolean) extends Value {
  override def isValid: Boolean = !opaqueTag.isEmpty

  override val value = if (isWeak) "W/" + quote(opaqueTag) else quote(opaqueTag)
}

//---------------------------------------------------------------------------------------------------------------------

object EntityTag {
  // http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11

  /** Parses a header value to extract a warning, if this is possible. */
  def apply(value: String) = {
    if (value == null || value.length == 0)
      new EntityTag("", false)
    else if (value.startsWith("W/"))
      new EntityTag(unquote(value.substring(2)), true)
    else
      new EntityTag(unquote(value), false)
  }

  def ifValid(value: String) = {
    val v = apply(value)
    if (v.isValid) Some(v) else None
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a comma-separated list of entity-tag values.
 */
case class EntityTagListValue(parts: List[EntityTag]) extends ListValue(parts, ", ") with Value {

  override lazy val isValid: Boolean = parts forall(_.isValid)
  override lazy val value = parts map(_.value) mkString ", "
}

//---------------------------------------------------------------------------------------------------------------------

object EntityTagListValue {
  def apply(string: String) = {
    val list = splitQuoted(string, ',')
    val parts = list map (s => EntityTag(s.trim))
    new EntityTagListValue(parts)
  }

  def ifValid(value: String) = {
    val v = apply(value)
    if (v.isValid) Some(v) else None
  }
}