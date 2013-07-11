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
import uk.co.bigbeeconsultants.http.util.HttpUtil
import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.charset.MalformedInputException

class InputStreamResponseBodyTest extends FunSuite with ShouldMatchers {
  val head = Request.head(new URL("http://localhost/"))
  val headersWithLength = Headers(HeaderName.CONTENT_LENGTH -> "1234")

  test("InputStreamResponseBody in unbuffered state with json body") {
    val s = """[ "Some json message text" ]"""
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    body.isBuffered should be(false)
    body.inputStream should be(bais)
    body.contentType should be(mt)
    body.isTextual should be(true)

    intercept[IllegalStateException] {
      body.asString
    }
    intercept[IllegalStateException] {
      body.asBytes
    }
    intercept[IllegalStateException] {
      body.contentLength
    }
    intercept[IllegalStateException] {
      body.asBytes
    }
    body.toString should be("(unbuffered application/json input stream)")
    body.inputStream should be(bais)
    body.close() // no exception
  }

  test("InputStreamResponseBody in buffered state with json body") {
    val s = """[ "Some json message text" ]"""
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // buffered state
    body.toBufferedBody
    body.isBuffered should be(true)
    body.contentType should be(mt)
    body.isTextual should be(true)
    body.asString should be(s)
    body.asBytes should be(bytes)
    body.asString should be(s)
    body.toString should be(s + " (application/json)")
    body.contentLength should be(bytes.length)

    body.toStringBody.asString should be(s)

    intercept[IllegalStateException] {
      body.inputStream
    }
  }

  test("InputStreamResponseBody with stream filter") {
    val s = "[ \"Some json message text\",\n" + " 123 ]"
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    body.inputStream should be(bais)
    val buffer = HttpUtil.copyToByteArrayAndClose(body.transformedStream((s) => s.replace("Some", "Fantastic")))
    val result = new String(buffer, "UTF-8")
    result should be("[ \"Fantastic json message text\",\n" + " 123 ]")
  }

  test("InputStreamResponseBody unbuffered empty binary iterator") {
    val s = ""
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val bytes = s.getBytes("ASCII")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    body.iterator.hasNext should be(false)
  }

  test("InputStreamResponseBody unbuffered non-empty binary iterator cannot convert to string") {
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val bytes: Array[Byte] = new Array[Byte](256)
    for (i <- 0 until 256) {
      bytes(i) = i.toByte
    }
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // unbuffered state
    body.isBuffered should be(false)
    intercept[MalformedInputException] {
      body.iterator
    }
  }

  test("InputStreamResponseBody unbuffered text iterator") {
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

  test("InputStreamResponseBody buffered text iterator") {
    val s = "line one\nline two\nline three\n"
    val mt = MediaType.TEXT_PLAIN
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new InputStreamResponseBody(head, Status.S200_OK, Some(mt), headersWithLength, bais)

    // buffered state
    body.toBufferedBody
    body.isBuffered should be(true)
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
