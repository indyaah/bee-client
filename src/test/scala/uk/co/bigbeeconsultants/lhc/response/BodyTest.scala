package uk.co.bigbeeconsultants.lhc.response

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

import org.junit.Test
import org.junit.Assert._
import java.net.URL
import java.io.ByteArrayInputStream
import uk.co.bigbeeconsultants.lhc.header.MediaType

class BodyTest {

  val url1 = new URL("http://localhost/")

  @Test
  def bufferedBody() {
    val s = """[ "Some json message text" ]"""
    val bais = new ByteArrayInputStream(s.getBytes("UTF-8"))
    val body = new InputStreamBufferBody
    body.receiveData(MediaType.APPLICATION_JSON, bais)

    assertEquals(MediaType.APPLICATION_JSON, body.contentType)
    assertArrayEquals(s.getBytes("UTF-8"), body.asBytes)
    assertEquals(s, body.asString)
  }


  @Test
  def stringBody() {
    val s = """[ "Some json message text" ]"""
    val body = new StringBody(MediaType.APPLICATION_JSON, s)

    assertEquals(MediaType.APPLICATION_JSON, body.contentType)
    assertEquals(s, body.asString)
    assertArrayEquals(s.getBytes("UTF-8"), body.asBytes)
  }
}
