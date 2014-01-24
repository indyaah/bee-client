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

import java.text.ParseException
import org.slf4j.LoggerFactory

/**
 * Holds a number as typically used in an HTTP header. Such numbers are normally positive integers and may or may not
 * fit into a 32-bit representation.
 */
case class NumberValue(value: String) extends Value {

  private val logger = LoggerFactory.getLogger(getClass)
  private var valid = false
  private var _number = 0L
  try {
    _number = value.toLong
    // Number values are always positive integral numbers in HTTP headers.
    valid = _number >= 0
  } catch {
    case e: NumberFormatException =>
      logger.error("{}: failed to parse number. {}", Array(value, e.getMessage))
  }

  /** True iff the date was parsed correctly. Otherwise the values string does not represent a date correctly. */
  val isValid = valid

  /** Converts the value to a long. */
  def toLong = _number

  /** Converts the value to an integer. */
  def toInt = _number.toInt
}
