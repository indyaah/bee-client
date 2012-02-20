package uk.co.bigbeeconsultants.lhc.header

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

class HeaderTest {

  @Test
  def qualifier_toString() {
    val q = Qualifier("a", "b")
    assertEquals("a=b", q.toString)
  }

  @Test
  def value_toString() {
    assertEquals("v", Part("v").toString)
    val v1 = Part("v", List(Qualifier("a", "b")))
    assertEquals("v;a=b", v1.toString)
  }

  @Test
  def simple() {
    val h = Header("Accept-Ranges: bytes")
    assertEquals("Accept-Ranges", h.name)
    assertEquals("bytes", h.value)
    assertEquals("Accept-Ranges: bytes", h.toString)
    //Allow: GET, HEAD, PUT
  }

  @Test
  def value_toInt() {
    val h = Header("Content-Length: 123")
    assertEquals("Content-Length", h.name)
    assertEquals(123, h.toInt)
  }

  @Test
  def value_toLong() {
    val h = Header("Content-Length: 123")
    assertEquals("Content-Length", h.name)
    assertEquals(123, h.toLong)
  }

  @Test
  def oneQ() {
    val h = Header("Accept: audio/*;q=0.2, audio/basic")
    val v = h.toQualifiedValue
    assertEquals("Accept", h.name)
    assertEquals(2, v.parts.size)
    assertEquals("audio/*", v.parts(0).value)
    assertEquals("q", v.parts(0).qualifier(0).label)
    assertEquals("0.2", v.parts(0).qualifier(0).value)
    assertEquals("audio/basic", v.parts(1).value)
    assertEquals("Accept: audio/*;q=0.2, audio/basic", h.toString)
    assertEquals("audio/*;q=0.2, audio/basic", v.toString)
  }

  @Test
  def complexQ() {
    val h = Header("Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")
    val v = h.toQualifiedValue
    assertEquals("Accept", h.name)
    assertEquals(5, v.parts.size)
    assertEquals("text/*", v.parts(0).value)
    assertEquals("q", v.parts(0).qualifier(0).label)
    assertEquals("0.3", v.parts(0).qualifier(0).value)
    assertEquals("text/html", v.parts(3).value)
    assertEquals("level", v.parts(3).qualifier(0).label)
    assertEquals("2", v.parts(3).qualifier(0).value)
    assertEquals("q", v.parts(3).qualifier(1).label)
    assertEquals("0.4", v.parts(3).qualifier(1).value)
    assertEquals("Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5", h.toString)
    assertEquals("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5", v.toString)
  }

  @Test
  def date() {
    val time = DatatypeConverter.parseDateTime("1994-11-06T08:49:37Z").getTime
    val h = Header("Date: Sun, 06 Nov 1994 08:49:37 GMT")
    assertEquals("Date", h.name)
    assertEquals(time, h.toDate().date)
  }

}
