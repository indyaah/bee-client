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

import uk.co.bigbeeconsultants.http.header.MediaType
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.bigbeeconsultants.http.HttpClient
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}

class ResponseBodyTest extends FunSuite with ShouldMatchers {

  test("CopiedByteBufferResponseBody") {
    val s = """[ "Some json message text" ]"""
    val mt = MediaType.APPLICATION_JSON
    val bytes = s.getBytes("UTF-8")
    val bais = new ByteArrayInputStream(bytes)
    val body = new CopiedByteBufferResponseBody

    body.receiveData(mt, bais)

    body.contentType should be(mt)
    body.asBytes should be(bytes)
    body.toString should be(s)
  }


  test("StringResponseBody") {
    val s = """[ "Some json message text" ]"""
    val bytes = s.getBytes(HttpClient.UTF8)
    val mt = MediaType.APPLICATION_JSON
    val body = new StringResponseBody(mt, s)

    body.contentType should be(mt)
    body.asBytes should be(bytes)
    body.toString should be(s)
  }


  test("CopyStreamResponseBody") {
    val s = """So shaken as we are, so wan with care!"""
    val baos = new ByteArrayOutputStream
    val inputStream = new ByteArrayInputStream(s.getBytes(HttpClient.UTF8))
    val mt = MediaType.TEXT_PLAIN
    val body = new CopyStreamResponseBody(baos)

    body.receiveData(mt, inputStream)

    body.contentType should be(mt)
    val result = new String(baos.toByteArray, HttpClient.UTF8)
    result should be (s)
  }
}
