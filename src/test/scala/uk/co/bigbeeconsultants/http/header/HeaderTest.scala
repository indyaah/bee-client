package uk.co.bigbeeconsultants.http.header

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

import javax.xml.bind.DatatypeConverter
import org.scalatest.FunSuite

class HeaderTest extends FunSuite {


  test ("qualifier_toString") {
    val q = Qualifier ("a", "b")
    expect ("a=b")(q.toString)
  }


  test ("value_toString") {
    expect ("v")(Part ("v").toString)
    val v1 = Part ("v", List (Qualifier ("a", "b")))
    expect ("v;a=b")(v1.toString)
  }


  test ("simple") {
    val h = Header ("Accept-Ranges: bytes")
    expect ("Accept-Ranges")(h.name)
    expect ("bytes")(h.value)
    expect ("Accept-Ranges: bytes")(h.toString)
    //Allow: GET, HEAD, PUT
  }


  test ("value_toInt") {
    val h = Header ("Content-Length: 123")
    expect ("Content-Length")(h.name)
    expect (123)(h.toInt)
  }


  test ("value_toLong") {
    val h = Header ("Content-Length: 123")
    expect ("Content-Length")(h.name)
    expect (123)(h.toLong)
  }


  test ("oneQ") {
    val h = Header ("Accept: audio/*;q=0.2, audio/basic")
    val v = h.toQualifiedValue
    expect ("Accept")(h.name)
    expect (2)(v.parts.size)
    expect ("audio/*")(v.parts (0).value)
    expect ("q")(v.parts (0).qualifier (0).label)
    expect ("0.2")(v.parts (0).qualifier (0).value)
    expect ("audio/basic")(v.parts (1).value)
    expect ("Accept: audio/*;q=0.2, audio/basic")(h.toString)
    expect ("audio/*;q=0.2, audio/basic")(v.toString)
  }


  test ("complexQ") {
    val h = Header ("Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")
    val v = h.toQualifiedValue
    expect ("Accept")(h.name)
    expect (5)(v.parts.size)
    expect ("text/*")(v.parts (0).value)
    expect ("q")(v.parts (0).qualifier (0).label)
    expect ("0.3")(v.parts (0).qualifier (0).value)
    expect ("text/html")(v.parts (3).value)
    expect ("level")(v.parts (3).qualifier (0).label)
    expect ("2")(v.parts (3).qualifier (0).value)
    expect ("q")(v.parts (3).qualifier (1).label)
    expect ("0.4")(v.parts (3).qualifier (1).value)
    expect ("Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")(h.toString)
    expect ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")(v.toString)
  }


  test ("date") {
    val time = DatatypeConverter.parseDateTime ("1994-11-06T08:49:37Z").getTime
    val h = Header ("Date: Sun, 06 Nov 1994 08:49:37 GMT")
    expect ("Date")(h.name)
    expect (time)(h.toDate ().date)
  }

}
