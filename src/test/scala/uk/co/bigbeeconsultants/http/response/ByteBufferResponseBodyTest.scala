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

package uk.co.bigbeeconsultants.http.response

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.bigbeeconsultants.http.header.MediaType
import java.io.ByteArrayInputStream


class ByteBufferResponseBodyTest extends FunSuite with ShouldMatchers {

  test("ByteBufferResponseBody with json body") {
    val s = """[ "Some json message text" ]"""
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new ByteBufferResponseBody(mt, bais)

    body.contentType should be(mt)
    body.asBytes should be(bytes)
    body.toString should be(s)
  }

  test("ByteBufferResponseBody with text but without a body") {
    val mt = MediaType.APPLICATION_JSON
    val body = new ByteBufferResponseBody(mt, null)

    body.contentType should be(mt)
    body.asBytes should be(new Array[Byte](0))
    body.toString should be("")
  }

  test("ByteBufferResponseBody with binary but without a body") {
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val body = new ByteBufferResponseBody(mt, null)

    body.contentType should be(mt)
    body.asBytes should be(new Array[Byte](0))
    body.toString should be("")
  }
}
