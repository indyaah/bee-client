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
import java.text.ParseException
import org.slf4j.LoggerFactory

/**
 * Holds a warning message, typically as attached by cache operation failures etc.
 * @param value the source header value string
 * @param isValid true if the source string was correctly parsed
 * @param code the warning code
 * @param agent the agent that created the warning
 * @param text the human-readable warning message
 * @param date the optional date when the warning was raised
 */
case class WarningValue(value: String, isValid: Boolean, code: Int, agent: String, text: String, date: Option[HttpDateTimeInstant])
  extends Value


object WarningValue {
  // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46
  // warning-value = warn-code SP warn-agent SP "warn-text" [SP "warn-date"]

  private val logger = LoggerFactory.getLogger(getClass)

  /** Parses a header value to extract a warning, if this is possible. */
  def apply(value: String) = {
    try {
      val parts = splitQuoted(value, ' ')
      if (parts.size < 3 || parts.size > 4)
        new WarningValue(value, false, 0, "", "", None)
      else {
        val code = parts(0).toInt
        val agent = parts(1)
        val text = unquote(parts(2))
        val date = if (parts.size == 4) Some(HttpDateTimeInstant.parse(unquote(parts(3)))) else None
        new WarningValue(value, true, code, agent, text, date)
      }
    } catch {
      case e: RuntimeException =>
        logger.error("{}: failed to parse warning. {}", Array(value, e.getMessage))
        new WarningValue(value, false, 0, "", "", None)
      case pe: ParseException =>
        logger.error("{}: failed to parse warning. {}", Array(value, pe.getMessage))
        new WarningValue(value, false, 0, "", "", None)
    }
  }

  /** Construct an instance from its constituent parts. */
  def apply(code: Int, agent: String, text: String, date: Option[HttpDateTimeInstant] = None) = {
    val dateStr = if (date.isDefined) " " + quote(date.get.toString) else ""
    val value = code + " " + agent + " " + quote(text) + dateStr
    new WarningValue(value, true, code, agent, text, date)
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Defines an HTTP header value consisting of a comma-separated list of warning values.
 */
case class WarningListValue(parts: List[WarningValue]) extends ListValue(parts, ", ") with Value {

  override lazy val isValid: Boolean = parts forall(_.isValid)
  override lazy val value = parts map(_.value) mkString ", "
}

//---------------------------------------------------------------------------------------------------------------------

object WarningListValue {
  def apply(string: String) = {
    val list = splitQuoted(string, ',')
    val parts = list map (s => WarningValue(s.trim))
    new WarningListValue(parts)
  }
}