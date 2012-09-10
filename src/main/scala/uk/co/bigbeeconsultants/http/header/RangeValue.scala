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
import java.util.regex.Pattern

/**
 * Defines an integer range, according to HTTP usage. E.g. "5-17" represents the range 5 to 17.
 */
case class Range(first: Option[Int], last: Option[Int]) {

  require (first.getOrElse (0) >= 0, first.toString)
  require (last.getOrElse (0) >= 0, last.toString)

  def isValid =
    if (first.isEmpty && last.isEmpty)
      false
    else if (first.isDefined && last.isEmpty)
      true
    else if (first.isEmpty && last.isDefined)
      true
    else
      first.get <= last.get

  /**Calculates the start,length tuple when this range is applied to an entity of a particular length. */
  def of(totalLength: Int): (Int, Int) = {
    require (isValid, toString)
    if (first.isEmpty && last.isEmpty)
      (0, totalLength)
    else if (first.isDefined && last.isEmpty)
      (first.get, totalLength - first.get)
    else if (first.isEmpty && last.isDefined)
      (totalLength - last.get, last.get) // the final 'last' bytes
    else
      (first.get, 1 + last.get - first.get)
  }

  private def str(cpt: Option[Int]) = if (cpt.isEmpty) "" else cpt.get.toString

  def firstBytePos = str (first)

  def lastBytePos = str (last)

  override def toString = if (first.isEmpty && last.isEmpty) "" else str (first) + "-" + str (last)
}

object Range {
  def apply(str: String) = {
    val t = HttpUtil.divide (str, '-')
    new Range (toOptInt (t._1), toOptInt (t._2))
  }

  private def toOptInt(num: String) = if (num.length == 0) None else Some (num.toInt)
}

/**
 * Provides a single component of a range value. This is either a Range or "bytes" or "none".
 */
case class RangePart(prefix: String, value: Either[Range, String]) {
  def isValid = if (value.isLeft)
    value.left.get.isValid
  else
    value.right.get == "bytes" || value.right.get == "none"

  override def toString = prefix + (if (value.isLeft) value.left.get.toString else value.right.get.toString)
}

object RangePart {
  private val numberPattern = Pattern.compile ("""[0-9\-]+""")

  def parse(str: String) = {
    val tr = str.trim
    if (tr.startsWith ("bytes=")) {
      doApply ("bytes=", tr.substring (6))
    } else {
      doApply ("", tr)
    }
  }

  private def doApply(prefix: String, str: String) = {
    if (numberPattern.matcher (str).matches ()) {
      val r = Range (str)
      if (r.isValid) new RangePart (prefix, Left (r)) else new RangePart (prefix, Right (str))
    } else {
      new RangePart (prefix, Right (str))
    }
  }
}


/**
 * Defines an HTTP header value for a list of ranges. Typically, such values are
 * used for the Accept-Ranges and Content-Ranges headers. Example value:
 {{{
 bytes=500-599,700-799
 }}}
 */
case class RangeValue(value: String) extends Value {

  val parts: List[RangePart] =
    HttpUtil.split (value, ',').map {
      v: String => RangePart.parse (v)
    }.toList

  def apply(i: Int) = parts (i).value

  def isValid = parts.forall (_.isValid)

  override val toString = parts.mkString (",")
}
