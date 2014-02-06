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

import org.slf4j.LoggerFactory

/**
 * Holds a number as typically used in an HTTP header. Such numbers are normally positive integers and may or may not
 * fit into a 32-bit representation.
 */
case class NumberValue(value: String, isValid: Boolean, toLong: Long) extends Value {
  /** Shortens the value to an integer. */
  def toInt = toLong.toInt
}


object NumberValue {
  private val logger = LoggerFactory.getLogger(getClass)

  def apply(value: String) = {
    val number = ifValid(value)
    if (number.isDefined)
      new NumberValue(value, true, number.get)
    else
      new NumberValue(value, false, 0)
  }

  def ifValid(value: String): Option[Long] = {
    try {
      val number = value.toLong
      // Number values are always positive integral numbers in HTTP headers.
      if (number >= 0) Some(number) else None
    } catch {
      case e: NumberFormatException =>
        logger.debug("{}: failed to parse number. {}", Array(value, e.getMessage))
        None
    }
  }
}