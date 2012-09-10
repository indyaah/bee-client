package uk.co.bigbeeconsultants.http

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

import javax.xml.bind.DatatypeConverter
import org.scalatest.FunSuite

class HttpDateTimeInstantTest extends FunSuite {

  test ("parse silly") {
    val d = HttpDateTimeInstant.parse ("some silly rubbish", HttpDateTimeInstant.zero)
    expect (HttpDateTimeInstant.zero)(d)
  }

  test ("parse rfc1123DateTimeFormat") {
    val exp = new HttpDateTimeInstant (DatatypeConverter.parseDateTime ("2005-11-16T08:49:37Z"))
    val dateString = "Wed, 16 Nov 2005 08:49:37 GMT"
    val d = HttpDateTimeInstant.parse (dateString)
    expect (exp)(d)
    expect (dateString)(d.toString)
  }

  test ("parse rfc850DateTimeFormat") {
    val exp = new HttpDateTimeInstant (DatatypeConverter.parseDateTime ("2005-11-16T08:49:37Z"))
    val d = HttpDateTimeInstant.parse ("Wednesday, 16-Nov-05 08:49:37 GMT")
    expect (exp)(d)
  }

  test ("parse asciiDateTimeFormat1") {
    val exp = new HttpDateTimeInstant (DatatypeConverter.parseDateTime ("1994-11-06T08:49:37Z"))
    val d = HttpDateTimeInstant.parse ("Sun Nov  6 08:49:37 1994")
    expect (exp)(d)
  }

  test ("parse asciiDateTimeFormat2") {
    val exp = new HttpDateTimeInstant (DatatypeConverter.parseDateTime ("2005-11-16T08:49:37Z"))
    val d = HttpDateTimeInstant.parse ("Wed Nov 16 08:49:37 2005")
    expect (exp)(d)
  }

  test ("plus") {
    val now = new HttpDateTimeInstant ()
    val later = new HttpDateTimeInstant () + 60
    expect (60)(later.seconds - now.seconds)
  }

  test ("minus") {
    val now = new HttpDateTimeInstant ()
    val later = new HttpDateTimeInstant () - 60
    expect (-60)(later.seconds - now.seconds)
  }

  test ("compare") {
    val now = new HttpDateTimeInstant ()
    val later = new HttpDateTimeInstant () + 60
    expect (true)(later > now)
  }
}
