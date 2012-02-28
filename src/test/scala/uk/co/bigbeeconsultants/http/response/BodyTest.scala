package uk.co.bigbeeconsultants.http.response

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

import java.io.ByteArrayInputStream
import uk.co.bigbeeconsultants.http.header.MediaType
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class BodyTest extends FunSuite with ShouldMatchers {

  test ("InputStreamBufferBody") {
    val s = """[ "Some json message text" ]"""
    val bytes = s.getBytes ("UTF-8")
    val bais = new ByteArrayInputStream (bytes)
    val body = new InputStreamBufferBody
    body.receiveData (MediaType.APPLICATION_JSON, bais)

    body.contentType should be (MediaType.APPLICATION_JSON)
    body.asBytes should be (bytes)
    body.toString should be (s)
  }



  test ("StringBody") {
    val s = """[ "Some json message text" ]"""
    val bytes = s.getBytes ("UTF-8")
    val body = new StringBody (MediaType.APPLICATION_JSON, s)

    body.contentType should be (MediaType.APPLICATION_JSON)
    body.asBytes should be (bytes)
    body.toString should be (s)
  }
}
