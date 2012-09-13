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
import java.net.URL


class ByteBufferResponseBodyTest extends FunSuite with ShouldMatchers {

  test("ByteBufferResponseBody with json body") {
    val s = """[ "Some json message text" ]"""
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new ByteBufferResponseBody(None, Some(mt), bais)

    body.contentType should be(mt)
    body.contentLength should be(bytes.length)
    body.asBytes should be(bytes)
    body.isTextual should be(true)
    body.toString should be(s)
  }

  test("ByteBufferResponseBody with text but without a body") {
    val mt = MediaType.APPLICATION_JSON
    val body = new ByteBufferResponseBody(None, Some(mt), null)

    body.contentType should be(mt)
    body.contentLength should be(0)
    body.asBytes should be(new Array[Byte](0))
    body.isTextual should be(true)
    body.toString should be("")
  }

  test("ByteBufferResponseBody with binary but without a body") {
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val body = new ByteBufferResponseBody(None, Some(mt), null)

    body.contentType should be(mt)
    body.contentLength should be(0)
    body.asBytes should be(new Array[Byte](0))
    body.isTextual should be(false)
    body.toString should be("")
  }

  test("ByteBufferResponseBody with binary and a body") {
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val bytes = Array[Byte](' ')
    val bais = new ByteArrayInputStream(bytes)
    val body = new ByteBufferResponseBody(None, Some(mt), bais)

    body.contentType should be(mt)
    body.contentLength should be(1)
    body.asBytes should be(bytes)
    body.isTextual should be(false)
    body.toString should be("")
  }

  test("ByteBufferResponseBody with plain text but no media type") {
    val s = "Some text"
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new ByteBufferResponseBody(None, None, bais)

    body.contentType should be(MediaType.TEXT_PLAIN)
    body.contentLength should be(bytes.length)
    body.asBytes should be(bytes)
    body.isTextual should be(true)
    body.toString should be(s)
  }

  test("ByteBufferResponseBody with html text but no media type") {
    val s = "<html><body>Blah</body></html>"
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new ByteBufferResponseBody(None, None, bais)

    body.contentType should be(MediaType.TEXT_HTML)
    body.contentLength should be(bytes.length)
    body.asBytes should be(bytes)
    body.isTextual should be(true)
    body.toString should be(s)
  }

  test("ByteBufferResponseBody with binary but no media type") {
    val bytes: Array[Byte] = new Array[Byte](256)
    for (i <- 0 until 256) { bytes(i) = i.toByte }
    val bais = new ByteArrayInputStream(bytes)
    val body = new ByteBufferResponseBody(None, None, bais)

    body.contentType should be(MediaType.APPLICATION_OCTET_STREAM)
    body.contentLength should be(256)
    body.asBytes should be(bytes)
    body.isTextual should be(false)
    body.toString should be("")
  }

  test("ByteBufferResponseBody with binary but without a body or media type") {
    val body = new ByteBufferResponseBody(None, None, null)

    body.contentType should be(MediaType.APPLICATION_OCTET_STREAM)
    body.contentLength should be(0)
    body.asBytes should be(new Array[Byte](0))
    body.isTextual should be(false)
    body.toString should be("")
  }

  test("ByteBufferResponseBody with PNG but without a body or media type") {
    val body = new ByteBufferResponseBody(Some(new URL("http://localhost/x.png")), None, null)

    body.contentType should be(MediaType.IMAGE_PNG)
    body.contentLength should be(0)
    body.asBytes should be(new Array[Byte](0))
    body.isTextual should be(false)
    body.toString should be("")
  }

}
