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
import uk.co.bigbeeconsultants.http.header.MediaType
import java.io.ByteArrayInputStream

class StringResponseBodyTest extends FunSuite {

  test("utf8 encoding") {
    val bytes = Array(97, 195, 190, 226, 130, 172) map (_.toByte)
    val s = "aþ€"
    val srb = new StringResponseBody(s, MediaType.TEXT_PLAIN)
    assert(srb.asBytes === bytes)
  }

  test("StringResponseBody iterator ending without newline") {
    val s = "line one\nline two\nline three"
    val mt = MediaType.TEXT_PLAIN
    val body = new StringResponseBody(s, mt)

    assert(body.isBuffered === true)
    val it = body.iterator
    assert(it.next() === "line one")
    assert(it.next() === "line two")
    assert(it.next() === "line three")
    assert(it.hasNext === false)
  }

  test("StringResponseBody iterator ending in newline") {
    val s = "line one\n"
    val mt = MediaType.TEXT_PLAIN
    val body = new StringResponseBody(s, mt)

    assert(body.isBuffered === true)
    val it = body.iterator
    assert(it.next() === "line one")
    assert(it.next() === "")
    assert(it.hasNext === false)
  }

  test("StringResponseBody empty iterator") {
    val s = ""
    val mt = MediaType.TEXT_PLAIN
    val body = new StringResponseBody(s, mt)

    assert(body.isBuffered === true)
    val it = body.iterator
    assert(it.hasNext === false)
  }

  test("StringResponseBody empty binary iterator") {
    val s = "line one\n"
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val body = new StringResponseBody(s, mt)

    assert(body.isBuffered === true)
    val it = body.iterator
    assert(it.hasNext === false)
  }

  test("StringResponseBody conversion round trip") {
    val s = "line one\n"
    val mt = MediaType.TEXT_PLAIN
    val body = new StringResponseBody(s, mt)
    val bb = body.toBufferedBody.asInstanceOf[ByteBufferResponseBody]

    assert(body.toStringBody === body)
    assert(body.asString === s)
    assert(bb.asString === s)
    assert(bb.toStringBody === body)
    assert(bb === body.toBufferedBody)
  }
}
