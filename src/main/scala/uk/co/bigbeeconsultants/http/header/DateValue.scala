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
 * Holds a header value that refers to a date.
 */
case class DateValue(value: String, isValid: Boolean, date: HttpDateTimeInstant) extends Value


object DateValue extends ValueParser[HttpDateTimeInstant] {

  private val logger = LoggerFactory.getLogger(getClass)

  /** Parses a header value to extract a date value, if this is possible. */
  def apply(value: String) = {
    val date = ifValid(value)
    if (date.isDefined) new DateValue(value, true, date.get)
    else new DateValue(value, false, HttpDateTimeInstant.Zero)
  }

  /** Parses a header value to extract a date value, if this is possible. */
  def ifValid(value: String): Option[HttpDateTimeInstant] = {
    try {
      Some(HttpDateTimeInstant.parse(value))
    } catch {
      case pe: ParseException =>
        logger.debug("{}: failed to parse date. {}", Array(value, pe.getMessage))
        None
    }
  }
}