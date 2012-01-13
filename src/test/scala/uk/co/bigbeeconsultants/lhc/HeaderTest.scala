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

import org.junit.Test
import org.junit.Assert._

class HeaderTest {

  @Test
  def qualifier_toString() {
    val q = Qualifier("a", "b")
    assertEquals("a=b", q.toString)
  }

  @Test
  def value_toString() {
    assertEquals("v", Value("v").toString)
    val v1 = Value("v", List(Qualifier("a", "b")))
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
  def oneQ() {
    val h = Header("Accept: audio/*; q=0.2, audio/basic")
    assertEquals("Accept", h.name)
    assertEquals(2, h.values.size)
    assertEquals("audio/*", h.values(0).value)
    assertEquals("q", h.values(0).qualifier(0).label)
    assertEquals("0.2", h.values(0).qualifier(0).value)
    assertEquals("audio/basic", h.values(1).value)
    assertEquals("Accept: audio/*;q=0.2, audio/basic", h.toString)
  }

  @Test
  def complexQ() {
    val h = Header("Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")
    assertEquals("Accept", h.name)
    assertEquals(5, h.values.size)
    assertEquals("text/*", h.values(0).value)
    assertEquals("q", h.values(0).qualifier(0).label)
    assertEquals("0.3", h.values(0).qualifier(0).value)
    assertEquals("text/html", h.values(3).value)
    assertEquals("level", h.values(3).qualifier(0).label)
    assertEquals("2", h.values(3).qualifier(0).value)
    assertEquals("q", h.values(3).qualifier(1).label)
    assertEquals("0.4", h.values(3).qualifier(1).value)
    assertEquals("Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5", h.toString)
  }

}
