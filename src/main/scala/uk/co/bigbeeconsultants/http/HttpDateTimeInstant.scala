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

package uk.co.bigbeeconsultants.http

import java.text.SimpleDateFormat
import java.util.{TimeZone, Calendar, Date}
import java.text.ParseException
import org.slf4j.LoggerFactory

/**
 * Expresses the number of seconds since 1st Jan 1970, as used in HTTP date headers.
 */
case class HttpDateTimeInstant(seconds: Long) extends Ordered[HttpDateTimeInstant] {

  import HttpDateTimeInstant._

  /** Converts a Java date into an instance of HttpDateTimeInstant. */
  def this(d: Date) = this(d.getTime / 1000)

  /** Converts a Java calendar into an instance of HttpDateTimeInstant. */
  def this(c: Calendar) = this(c.getTime)

  /** Creates an instance of HttpDateTimeInstant representing the time now. */
  def this() = this(System.currentTimeMillis() / 1000)

  /** This instant converted to a Java date. */
  lazy val date = new Date(seconds * 1000)

  /** Adds some seconds to this instant, returning a new instance. */
  def +(secondsDelta: Long) = if (secondsDelta == 0) this else new HttpDateTimeInstant(seconds + secondsDelta)

  /** Subtracts some seconds off this instant, returning a new instance. */
  def -(secondsDelta: Long) = if (secondsDelta == 0) this else new HttpDateTimeInstant(seconds - secondsDelta)

  /** Formats this instant as a string in full RFC1123 format. */
  override lazy val toString = {
    formatString(fullRfc1123DateTimeFormat) + " GMT"
  }

  def toIsoString = {
    formatString(iso8601DateTimeFormat)
  }

  def formatString(format: String) = {
    val df = new SimpleDateFormat(format)
    df.setTimeZone(HttpDateTimeInstant.GMT)
    df.format(date)
  }

  /** Implements ordering of instances. */
  def compare(that: HttpDateTimeInstant) = seconds.compare(that.seconds)
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * HttpDateTimeInstant expresses the number of seconds since 1st Jan 1970, as used in HTTP date
 * headers. This companion object provides hand-optimised parsing functions.
 */
object HttpDateTimeInstant {
  final val logger = LoggerFactory.getLogger(getClass)

  private val monthsEN = List("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

  /**
   * RFC1123 dates are always in GMT. The canonical representation is rfc1123DateTimeFormat,
   * in which the leading "EEE, " is assumed to have been stripped, and the trailing "GMT" is ignored.
   * That is, the representation is "dd MMM yyyy HH:mm:ss".
   */
  val rfc1123DateTimeFormat = "dd MMM yyyy HH:mm:ss"

  // Some servers seem to get RFC1123 wrong and use dashes instead, so we have this.
  private val altRfc1123DateTimeFormat = "dd-MMM-yyyy HH:mm:ss"

  /**
   * The same as rfc1123DateTimeFormat except with the leading "EEE, " day name.
   */
  val fullRfc1123DateTimeFormat = "EEE, " + rfc1123DateTimeFormat

  // leading "EEEE, " assumed to have been stripped; trailing "GMT" ignored
  private val rfc850DateTimeFormat = "dd-MMM-yy HH:mm:ss"

  // leading "EEE " assumed to have been stripped; trailing "GMT" ignored
  private val asciiDateTimeFormat = "MMM d HH:mm:ss yyyy"

  /**
   * ISO8601 defines this widely used format. Ironically, it isn't used in HTTP but is included here as
   * a useful extension. This particular variant is only suitable for UTC ('Zulu').
   * "yyyy-MM-dd'T'HH:mm:ss'Z'"
   */
  val iso8601DateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  val zero = new HttpDateTimeInstant(0)

  /**
   * Parses a string as a date-time instant of the syntactic form expected in HTTP. If the dateString cannot
   * be parsed because its syntax is incorrect, the default value is returned instead.
   */
  def parse(dateString: String, defaultValue: HttpDateTimeInstant): HttpDateTimeInstant = {
    var result = defaultValue
    try {
      result = parse(dateString)
    }
    catch {
      case e: Exception => {
        logger.error("{}: failed to parse date. {}", dateString, e.getMessage)
      }
    }
    result
  }

  /**
   * Parses a string as a date-time instant of the syntactic form expected in HTTP.
   */
  @throws(classOf[ParseException])
  def parse(dateString: String): HttpDateTimeInstant = {
    val discriminant = dateString.charAt(3)
    if (discriminant == ',') {
      quickParseRfc1123(dateString)
//      slowParseRfc1123(dateString)
    }
    else if (discriminant == ' ') {
      val df = new SimpleDateFormat(asciiDateTimeFormat)
      df.setTimeZone(GMT)
      new HttpDateTimeInstant(df.parse(dateString.substring(4)))
    }
    else {
      val df = new SimpleDateFormat(rfc850DateTimeFormat)
      df.setTimeZone(GMT)
      new HttpDateTimeInstant(df.parse(dateString.substring(dateString.indexOf(' ') + 1)))
    }
  }

  // This parser uses the standard API for robustness but is slow
  private[http] def slowParseRfc1123(dateString: String): HttpDateTimeInstant = {
    val fmt = if (dateString.charAt(7) == '-') altRfc1123DateTimeFormat else rfc1123DateTimeFormat
    val df = new SimpleDateFormat(fmt)
    df.setTimeZone(GMT)
    new HttpDateTimeInstant(df.parse(dateString.substring(5)))
  }

  // This parser uses a quick conversion approach based on fixed fields to achieve significantly faster parsing
  private[http] def quickParseRfc1123(dateString: String): HttpDateTimeInstant = {
    val str = dateString.substring(5)
    var s = 0
    while (s < str.length && !Character.isDigit(str.charAt(s))) { s += 1 }
    var e = s
    while (e < str.length && Character.isDigit(str.charAt(e))) { e += 1 }
    val dd = str.substring(s, e).toInt

    s = e + 1
    while (s < str.length && !Character.isLetter(str.charAt(s))) { s += 1 }
    e = s
    while (e < str.length && Character.isLetter(str.charAt(e))) { e += 1 }
    val mmm = monthsEN.indexOf(str.substring(s, e))

    s = e + 1
    while (s < str.length && !Character.isDigit(str.charAt(s))) { s += 1 }
    e = s
    while (e < str.length && Character.isDigit(str.charAt(e))) { e += 1 }
    val yyyy = str.substring(s, e).toInt

    s = e + 1
    while (s < str.length && !Character.isDigit(str.charAt(s))) { s += 1 }
    e = s
    while (e < str.length && Character.isDigit(str.charAt(e))) { e += 1 }
    val thh = str.substring(s, e).toInt

    s = e + 1
    while (s < str.length && !Character.isDigit(str.charAt(s))) { s += 1 }
    e = s
    while (e < str.length && Character.isDigit(str.charAt(e))) { e += 1 }
    val tmm = str.substring(s, e).toInt

    s = e + 1
    while (s < str.length && !Character.isDigit(str.charAt(s))) { s += 1 }
    e = s
    while (e < str.length && Character.isDigit(str.charAt(e))) { e += 1 }
    val tss = str.substring(s, e).toInt

    val cal = Calendar.getInstance(GMT)
    cal.set(yyyy, mmm, dd, thh, tmm, tss)
    new HttpDateTimeInstant(cal)
  }

  private val GMT = TimeZone.getTimeZone("GMT")
}
