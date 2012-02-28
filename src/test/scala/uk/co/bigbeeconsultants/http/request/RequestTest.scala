package uk.co.bigbeeconsultants.http.request

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

import java.net.URL
import uk.co.bigbeeconsultants.http.header.MediaType
import org.scalatest.FunSuite

class RequestTest extends FunSuite {

  val url1 = new URL ("http://localhost/")

  test ("requestNoBody") {
    val r = Request.get (url1)
    expect (url1)(r.url)
    expect ("GET")(r.method)
    expect (true)(r.body.isEmpty)
  }

  test ("RequestWithBody") {
    val mt = MediaType.APPLICATION_JSON
    val b = Body (mt, "[1, 2, 3]")
    val r = Request.put (url1, b)
    expect (url1)(r.url)
    expect ("PUT")(r.method)
    expect (b)(r.body.get)
    expect ("UTF-8")(r.body.get.mediaType.charsetOrElse ("UTF-8"))
  }
}
