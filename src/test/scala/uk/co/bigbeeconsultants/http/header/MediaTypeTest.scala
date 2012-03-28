package uk.co.bigbeeconsultants.http.header

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

import org.scalatest.FunSuite

class MediaTypeTest extends FunSuite {


  test ("mediaType_constructionVariants") {
    expect ("application/json")(MediaType.APPLICATION_JSON.toString)
    expect ("text/plain")(MediaType ("text/plain").toString)
    expect (true)(MediaType.STAR_STAR.isWildcardType)
    expect (true)(MediaType.STAR_STAR.isWildcardSubtype)
    expect (false)(MediaType.TEXT_PLAIN.isWildcardType)
    expect (false)(MediaType.TEXT_PLAIN.isWildcardSubtype)
  }


  test ("parser") {
    val mt = MediaType ("text/html; charset=ISO-8859-1")
    expect ("text/html;charset=ISO-8859-1")(mt.toString)
    expect ("text")(mt.`type`)
    expect ("html")(mt.subtype)
    expect ("ISO-8859-1")(mt.charset.get)
  }


  test ("edgeCases") {
    expect ("text/*")(MediaType ("text/").toString)
    expect ("*/x")(MediaType ("/x").toString)
    expect ("*/*")(MediaType ("/").toString)
    expect ("*/*")(MediaType ("").toString)
  }


  test ("isCompatible") {
    val mt = MediaType ("text/html")
    expect (true)(mt.isCompatible (MediaType ("text/*")))
    expect (true)(mt.isCompatible (MediaType ("*/*")))
    expect (true)(mt.isCompatible (MediaType ("*/html")))
    expect (true)(mt.isCompatible (MediaType ("text/html")))
    expect (false)(mt.isCompatible (MediaType ("text/plain")))
    expect (false)(mt.isCompatible (MediaType ("image/*")))
    expect (false)(mt.isCompatible (null))
  }


  test ("isTextual") {
    expect (false)(MediaType.APPLICATION_OCTET_STREAM.isTextual)
    expect (false)(MediaType.IMAGE_JPG.isTextual)
    expect (false)(MediaType.STAR_STAR.isTextual)
    expect (true)(MediaType.TEXT_PLAIN.isTextual)
    expect (true)(MediaType.TEXT_HTML.isTextual)
    expect (true)(MediaType.APPLICATION_JSON.isTextual)
    expect (true)(MediaType.APPLICATION_XML.isTextual)
    expect (true)(MediaType.APPLICATION_SVG_XML.isTextual)
  }


  test ("withCharset") {
    val mt1 = MediaType.TEXT_HTML
    expect (true)(mt1.charset.isEmpty)
    val mt2 = mt1.withCharset ("UTF-8")
    expect ("UTF-8")(mt2.charset.get)
  }

  test ("withNullCharset") {
    intercept[IllegalArgumentException] {
      MediaType.TEXT_HTML.withCharset (null)
    }
  }
}
