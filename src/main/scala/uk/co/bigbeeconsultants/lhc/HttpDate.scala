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

import java.util.Date
import java.text.SimpleDateFormat
import com.weiglewilczek.slf4s.Logging

object HttpDate extends Logging {

  // Dates are always in GMT. The canonical representation is rfc1123DateTimeFormat.
  // leading "EEE, " assumed to have been stripped; trailing "GMT" ignored
  val rfc1123DateTimeFormat = "dd MMM yyyy HH:mm:ss"

  val fullRfc1123DateTimeFormat = "EEE, " + rfc1123DateTimeFormat

  // leading "EEEE, " assumed to have been stripped; trailing "GMT" ignored
  val rfc850DateTimeFormat = "dd-MMM-yy HH:mm:ss"

  // leading "EEE " assumed to have been stripped; trailing "GMT" ignored
  val asciiDateTimeFormat = "MMM d HH:mm:ss yyyy"

  val default = new Date(0)

  def parse(dateString: String, defaultValue: Date = default): Date = {
    var result = defaultValue
    try {
      val discriminant = dateString.charAt(3)
      if (discriminant == ',') {
        val str = dateString.substring(5)
        result = new SimpleDateFormat(rfc1123DateTimeFormat).parse(str)
      }
      else if (discriminant == ' ') {
        val str = dateString.substring(4)
        result = new SimpleDateFormat(asciiDateTimeFormat).parse(str)
      }
      else {
        val p = dateString.indexOf(' ')
        val str = dateString.substring(p + 1)
        result = new SimpleDateFormat(rfc850DateTimeFormat).parse(str)
      }
    }
    catch {
      case e: Exception => {
        logger.error(dateString + ": failed to parse date. " + e.getMessage)
      }
    }
    result
  }

  def format(date: Date): String = {
    new SimpleDateFormat(fullRfc1123DateTimeFormat).format(date) + " GMT"
  }
}
