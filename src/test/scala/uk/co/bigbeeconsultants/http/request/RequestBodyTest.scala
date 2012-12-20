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

package uk.co.bigbeeconsultants.http.request

import java.net.URL
import collection.immutable.ListMap
import uk.co.bigbeeconsultants.http.header.MediaType
import uk.co.bigbeeconsultants.http.HttpClient
import org.scalatest.FunSuite
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class RequestBodyTest extends FunSuite {

  val url1 = new URL("http://localhost/")

  test("empty body") {
    val mt = MediaType.APPLICATION_JSON
    val b = RequestBody(mt)
    assert(b.contentType === mt)
    val baos = new ByteArrayOutputStream
    b.copyTo(baos)
    val result = baos.toString(HttpClient.UTF8)
    assert(result === "")
    assert(b.toShortString === "(,application/json)")
    assert(b.toString === "RequestBody(,application/json)")
  }


  test("body with string") {
    val mt = MediaType.APPLICATION_JSON
    val str = "[1, 2, 3]"
    val b = RequestBody(str, mt)
    assert(b.contentType === mt)
    val baos = new ByteArrayOutputStream
    b.copyTo(baos)
    val result = baos.toString(HttpClient.UTF8)
    assert(result === str)
    assert(b.toShortString === "([1, 2, 3],application/json)")
    assert(b.toString === "RequestBody([1, 2, 3],application/json)")
  }


  test("body with input stream") {
    val s = "So shaken as we are, so wan with care!"
    val inputStream = new ByteArrayInputStream(s.getBytes(HttpClient.UTF8))
    val mt = MediaType.TEXT_PLAIN
    val b = RequestBody(inputStream, mt)
    assert(b.contentType === mt)
    val baos = new ByteArrayOutputStream
    b.copyTo(baos)
    val result = baos.toString(HttpClient.UTF8)
    assert(result === s)
    assert(b.toShortString === "(...,text/plain)")
  }


  test("body with binary data") {
    val s = "So shaken as we are, so wan with care!"
    val d = s.getBytes(HttpClient.UTF8)
    val mt = MediaType.APPLICATION_OCTET_STREAM
    val b = RequestBody(d, mt)
    assert(b.contentType === mt)
    val baos = new ByteArrayOutputStream
    b.copyTo(baos)
    val result = baos.toString(HttpClient.UTF8)
    assert(result === s)
    assert(b.toShortString === "(...,application/octet-stream)")
  }


  test("body with input stream fails if not cached") {
    val s = "So shaken as we are, so wan with care!"
    val inputStream = new ByteArrayInputStream(s.getBytes(HttpClient.UTF8))
    val mt = MediaType.TEXT_PLAIN
    val b = RequestBody(inputStream, mt)
    assert(b.contentType === mt)
    val baos = new ByteArrayOutputStream
    b.copyTo(baos)
    intercept[IllegalStateException] {
      b.copyTo(baos)
    }
    assert(b.toShortString === "(...,text/plain)")
  }


  test("body with keyVal pairs") {
    val mt = MediaType.APPLICATION_JSON
    val b = RequestBody(ListMap("a" -> "1", "b" -> "2", "c" -> "3"), mt)
    assert(b.contentType === mt)
    val baos = new ByteArrayOutputStream
    b.copyTo(baos)
    val result = baos.toString(HttpClient.UTF8)
    assert(result === "a=1&b=2&c=3")
    assert(b.toShortString === "(a=1&b=2&c=3,application/json)")
  }
}
