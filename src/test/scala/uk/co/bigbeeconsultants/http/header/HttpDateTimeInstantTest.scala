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

import javax.xml.bind.DatatypeConverter
import org.scalatest.FunSuite
import uk.co.bigbeeconsultants.http.util.DiagnosticTimer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HttpDateTimeInstantTest extends FunSuite {

  test("parse silly") {
    val d = HttpDateTimeInstant.parse("some silly rubbish", HttpDateTimeInstant.Zero)
    assert(HttpDateTimeInstant.Zero === d)
  }

  test("parse rfc1123DateTimeFormat (with spaces) 1") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2005-11-16T08:49:37Z"))
    val dateString = "Wed, 16 Nov 2005 08:49:37 GMT"
    val t = new DiagnosticTimer
    var i = 0
    while (i < 100000) {
      HttpDateTimeInstant.parse(dateString)
      i += 1
    }
    println(t.duration)
    val d = HttpDateTimeInstant.parse(dateString)
    assert(exp === d)
    assert(dateString === d.toString)
    assert("2005-11-16T08:49:37Z" === d.toIsoString)
  }

  test("parse rfc1123DateTimeFormat (with spaces) 2") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2014-02-11T00:06:54Z"))
    val dateString = "Tue, 11 Feb 2014 00:06:54 GMT"
    val d = HttpDateTimeInstant.parse(dateString)
    assert(exp === d)
    assert(dateString === d.toString)
    assert("2014-02-11T00:06:54Z" === d.toIsoString)
  }

  test("parse rfc1123DateTimeFormat (with spaces) and 2-digit year") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2015-02-08T01:38:48Z"))
    val dateString = "Sun, 08 Feb 15 01:38:48 GMT"
    val d = HttpDateTimeInstant.parse(dateString)
    assert(exp === d)
    assert("Sun, 08 Feb 2015 01:38:48 GMT" === d.toString)
    assert("2015-02-08T01:38:48Z" === d.toIsoString)
  }

  test("parse rfc1123DateTimeFormat-like but with dashes instead") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2005-11-01T08:49:37Z"))
    val d = HttpDateTimeInstant.parse("Tue, 01-Nov-2005 08:49:37 GMT")
    assert(exp === d)
    assert("Tue, 01 Nov 2005 08:49:37 GMT" === d.toString)
    assert("2005-11-01T08:49:37Z" === d.toIsoString)
  }

  test("parse rfc1123DateTimeFormat-like with dashes and two-digit year") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2015-02-08T01:38:48Z"))
    val d = HttpDateTimeInstant.parse("Sun, 08-Feb-15 01:38:48 GMT")
    assert(exp === d)
    assert("Sun, 08 Feb 2015 01:38:48 GMT" === d.toString)
    assert("2015-02-08T01:38:48Z" === d.toIsoString)
  }

  test("parse rfc1123DateTimeFormat with (non-standard) range of Zulu timezones") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2014-02-11T00:06:54Z"))
    for (zulu <- List("GMT", "UT", "Z")) {
      val dateString = "Tue, 11 Feb 2014 00:06:54 " + zulu
      val d = HttpDateTimeInstant.parse(dateString)
      assert(exp === d)
      assert("Tue, 11 Feb 2014 00:06:54 GMT" === d.toString)
      assert("2014-02-11T00:06:54Z" === d.toIsoString)
    }
  }

  test("parse and format a whole year") {
    val start = 1325376000
    for (i <- 0 until 366) {
      val t = start + (86400 * i)
      val exp = new HttpDateTimeInstant(t).toString
      val d = HttpDateTimeInstant.parse(exp)
      assert(exp === d.toString)
      //println(exp)
    }
  }

  test("parse rfc850DateTimeFormat") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2005-11-16T08:49:37Z"))
    val d = HttpDateTimeInstant.parse("Wednesday, 16-Nov-05 08:49:37 GMT")
    assert(exp === d)
  }

  test("parse asciiDateTimeFormat1") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("1994-11-06T08:49:37Z"))
    val d = HttpDateTimeInstant.parse("Sun Nov  6 08:49:37 1994")
    assert(exp === d)
  }

  test("parse asciiDateTimeFormat2") {
    val exp = new HttpDateTimeInstant(DatatypeConverter.parseDateTime("2005-11-16T08:49:37Z"))
    val d = HttpDateTimeInstant.parse("Wed Nov 16 08:49:37 2005")
    assert(exp === d)
  }

  test("plus") {
    val now = new HttpDateTimeInstant()
    val later = new HttpDateTimeInstant() + 60
    assert(60 === (later.seconds - now.seconds))
  }

  test("minus") {
    val now = new HttpDateTimeInstant()
    val later = new HttpDateTimeInstant() - 60
    assert(-60 === (later.seconds - now.seconds))
  }

  test("compare") {
    val now = new HttpDateTimeInstant()
    val later = new HttpDateTimeInstant() + 60
    assert(true === (later > now))
  }
}
