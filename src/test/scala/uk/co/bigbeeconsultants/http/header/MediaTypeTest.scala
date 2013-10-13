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

import org.scalatest.FunSuite

class MediaTypeTest extends FunSuite {


  test("mediaType construction variants") {
    assert("application/json" === MediaType.APPLICATION_JSON.toString)
    assert("text/plain" === MediaType("text/plain").toString)
    assert(true === MediaType.STAR_STAR.isWildcardType)
    assert(true === MediaType.STAR_STAR.isWildcardSubtype)
    assert(false === MediaType.TEXT_PLAIN.isWildcardType)
    assert(false === MediaType.TEXT_PLAIN.isWildcardSubtype)
  }


  test("parser") {
    val mt = MediaType("text/html; charset=ISO-8859-1")
    assert("text/html;charset=ISO-8859-1" === mt.toString)
    assert("text/html;charset=ISO-8859-1" === mt.toQualifiers.toString)
    assert("text" === mt.mainType)
    assert("html" === mt.subtype)
    assert("ISO-8859-1" === mt.charset.get)
  }


  test("edge cases") {
    assert("text/*" === MediaType("text/").toString)
    assert("*/x" === MediaType("/x").toString)
    assert("*/*" === MediaType("/").toString)
    assert("*/*" === MediaType("").toString)
  }


  test("isCompatible") {
    val mt = MediaType("text/html")
    assert(mt.isCompatible(MediaType("text/*")))
    assert(mt.isCompatible(MediaType("*/*")))
    assert(mt.isCompatible(MediaType("*/html")))
    assert(mt.isCompatible(MediaType("text/html")))

    assert(!mt.isCompatible(MediaType("text/plain")))
    assert(!mt.isCompatible(MediaType("image/*")))
    assert(!mt.isCompatible(null))
  }


  test("isTextual") {
    assert(!MediaType.APPLICATION_OCTET_STREAM.isTextual)
    assert(!MediaType.APPLICATION_FORM_URLENCODED.isTextual)
    assert(!MediaType.IMAGE_JPG.isTextual)
    assert(!MediaType.STAR_STAR.isTextual)

    assert(MediaType.TEXT_PLAIN.isTextual)
    assert(MediaType.TEXT_HTML.isTextual)
    assert(MediaType.APPLICATION_JSON.isTextual)
    assert(MediaType.APPLICATION_JAVASCRIPT.isTextual)
    assert(MediaType.APPLICATION_ECMASCRIPT.isTextual)
    assert(MediaType.APPLICATION_XML.isTextual)
    assert(MediaType.APPLICATION_SVG_XML.isTextual)
    assert(MediaType.APPLICATION_XHTML_XML.isTextual)
  }


  test("withCharset") {
    val mt1 = MediaType.TEXT_HTML
    assert(true === mt1.charset.isEmpty)
    val mt2 = mt1.withCharset("UTF-8")
    assert("UTF-8" === mt2.charset.get)
  }

  test("withNullCharset") {
    intercept[IllegalArgumentException] {
      MediaType.TEXT_HTML.withCharset(null)
    }
  }
}
