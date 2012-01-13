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

class MediaTypeTest {

  @Test
  def mediaType_constructionVariants() {
    assertEquals("application/json", MediaType.APPLICATION_JSON.toString)
    assertEquals("text/plain", MediaType("text/plain").toString)
    assertTrue(MediaType.STAR_STAR.isWildcardType)
    assertTrue(MediaType.STAR_STAR.isWildcardSubtype)
    assertFalse(MediaType.TEXT_PLAIN.isWildcardType)
    assertFalse(MediaType.TEXT_PLAIN.isWildcardSubtype)
  }

  @Test
  def parser() {
    val mt = MediaType("text/html; charset=ISO-8859-1")
    assertEquals("text/html; charset=ISO-8859-1", mt.toString)
    assertEquals("text", mt.`type`)
    assertEquals("html", mt.subtype)
    assertEquals("ISO-8859-1", mt.charset.get)
  }

  @Test
  def edgeCases() {
    assertEquals("text/*", MediaType("text/").toString)
    assertEquals("*/x", MediaType("/x").toString)
    assertEquals("*/*", MediaType("/").toString)
    assertEquals("*/*", MediaType("").toString)
  }

  @Test
  def withCharset() {
    val mt1 = MediaType.TEXT_HTML
    assertTrue(mt1.charset.isEmpty)
    val mt2 = mt1.withCharset("UTF-8")
    assertEquals("UTF-8", mt2.charset.get)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def withNullCharset() {
    MediaType.TEXT_HTML.withCharset(null)
  }
}
