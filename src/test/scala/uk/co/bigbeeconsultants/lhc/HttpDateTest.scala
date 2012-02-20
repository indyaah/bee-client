package uk.co.bigbeeconsultants.lhc

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

import org.junit.Test
import org.junit.Assert._
import javax.xml.bind.DatatypeConverter

class HttpDateTest {

  @Test
  def parse_silly() {
    val d = HttpDate.parse("some silly rubbish")
    assertEquals(HttpDate.default, d)
  }

  @Test
  def parse_rfc1123DateTimeFormat() {
    val exp = DatatypeConverter.parseDateTime("2005-11-16T08:49:37Z").getTime
    val dateString = "Wed, 16 Nov 2005 08:49:37 GMT"
    val d = HttpDate.parse(dateString)
    assertEquals(exp, d)
    assertEquals(dateString, HttpDate.format(d))
  }

  @Test
  def parse_rfc850DateTimeFormat() {
    val exp = DatatypeConverter.parseDateTime("2005-11-16T08:49:37Z").getTime
    val d = HttpDate.parse("Wednesday, 16-Nov-05 08:49:37 GMT")
    assertEquals(exp, d)
  }

  @Test
  def parse_asciiDateTimeFormat1() {
    val exp = DatatypeConverter.parseDateTime("1994-11-06T08:49:37Z").getTime
    val d = HttpDate.parse("Sun Nov  6 08:49:37 1994")
    assertEquals(exp, d)
  }

  @Test
  def parse_asciiDateTimeFormat2() {
    val exp = DatatypeConverter.parseDateTime("2005-11-16T08:49:37Z").getTime
    val d = HttpDate.parse("Wed Nov 16 08:49:37 2005")
    assertEquals(exp, d)
  }

}