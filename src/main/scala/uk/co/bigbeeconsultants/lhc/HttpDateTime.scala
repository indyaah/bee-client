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

package uk.co.bigbeeconsultants.lhc

import java.text.SimpleDateFormat
import com.weiglewilczek.slf4s.Logging
import java.util.{Calendar, Date}

/**
 * Expresses the number of seconds since 1st Jan 1970, as used in HTTP date headers.
 */
case class HttpDateTime(seconds: Long) extends Ordered[HttpDateTime] {

  def this(d: Date) = this(d.getTime / 1000)

  def this(c: Calendar) = this(c.getTime)

  def this() = this(System.currentTimeMillis() / 1000)

  lazy val date = new Date(seconds * 1000)

  def +(secondsDelta: Long) = if (secondsDelta == 0) this else new HttpDateTime(seconds + secondsDelta)

  override lazy val toString = {
    new SimpleDateFormat(HttpDateTime.fullRfc1123DateTimeFormat).format(date) + " GMT"
  }

  def compare(that: HttpDateTime) = (this.seconds - that.seconds).toInt
}

object HttpDateTime extends Logging {

  // Dates are always in GMT. The canonical representation is rfc1123DateTimeFormat.
  // leading "EEE, " assumed to have been stripped; trailing "GMT" ignored
  val rfc1123DateTimeFormat = "dd MMM yyyy HH:mm:ss"

  val fullRfc1123DateTimeFormat = "EEE, " + rfc1123DateTimeFormat

  // leading "EEEE, " assumed to have been stripped; trailing "GMT" ignored
  val rfc850DateTimeFormat = "dd-MMM-yy HH:mm:ss"

  // leading "EEE " assumed to have been stripped; trailing "GMT" ignored
  val asciiDateTimeFormat = "MMM d HH:mm:ss yyyy"

  val zero = new HttpDateTime(0)

  def parse(dateString: String, defaultValue: HttpDateTime = zero): HttpDateTime = {
    var result = defaultValue
    try {
      val discriminant = dateString.charAt(3)
      if (discriminant == ',') {
        val str = dateString.substring(5)
        result = new HttpDateTime(new SimpleDateFormat(rfc1123DateTimeFormat).parse(str))
      }
      else if (discriminant == ' ') {
        val str = dateString.substring(4)
        result = new HttpDateTime(new SimpleDateFormat(asciiDateTimeFormat).parse(str))
      }
      else {
        val p = dateString.indexOf(' ')
        val str = dateString.substring(p + 1)
        result = new HttpDateTime(new SimpleDateFormat(rfc850DateTimeFormat).parse(str))
      }
    }
    catch {
      case e: Exception => {
        logger.error(dateString + ": failed to parse date. " + e.getMessage)
      }
    }
    result
  }
}
