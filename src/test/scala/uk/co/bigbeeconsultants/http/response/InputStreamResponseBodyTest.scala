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
import uk.co.bigbeeconsultants.http.header.{Headers, HeaderName, MediaType}
import uk.co.bigbeeconsultants.http.request.Request
import java.io.ByteArrayInputStream
import java.net.URL
import uk.co.bigbeeconsultants.http.util.HttpUtil
import java.nio.ByteBuffer

class InputStreamResponseBodyTest extends FunSuite with ShouldMatchers {
  val head = Request.head(new URL("http://localhost/"))
  val headersWithLength = Headers(HeaderName.CONTENT_LENGTH -> "1234")

  test("InputStreamResponseBody with json body") {
    val s = """[ "Some json message text" ]"""
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    body.rawStream should be(bais)

    // buffered state
    body.toBufferedBody
    body.isBuffered should be(true)
    body.contentType should be(mt)
    body.contentLength should be(bytes.length)
    body.isTextual should be(true)
    body.asString should be("")
    body.toString should be(s)

    body.toBufferedBody.contentType should be(mt)
    body.toBufferedBody.contentLength should be(bytes.length)
    body.toBufferedBody.asString should be(s)
    body.toBufferedBody.toString should be(s)
    val it = body.toBufferedBody.iterator
    it.hasNext should be(true)
    it.next should be(s)
    it.hasNext should be(false)
  }

  test("InputStreamResponseBody with stream filter") {
    val s = "[ \"Some json message text\",\n" + " 123 ]"
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    body.rawStream should be(bais)
    val buffer = HttpUtil.copyToByteBufferAndClose(body.transformedStream((s) => s.replace("Some", "Fantastic")))
    val result = new String(buffer.array(), "UTF-8")
    result should be("[ \"Fantastic json message text\",\n" + " 123 ]")
  }

  test("InputStreamResponseBody iterator") {
    val s = "line one\nline two\nline three\n"
    val mt = MediaType.TEXT_PLAIN
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    val it = body.iterator
    it.next() should be("line one")
    it.next() should be("line two")
    it.next() should be("line three")
    it.hasNext should be(false)
  }

  test("unknown content type") {
    val s = """[ "Some json message text" ]"""
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, None, headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    body.contentType should be(MediaType.APPLICATION_OCTET_STREAM)
  }

}
