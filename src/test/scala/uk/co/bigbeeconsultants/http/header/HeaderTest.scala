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


  test ("qualifier toString") {
    val q = NameVal ("a", Some("b"))
    assert ("a=b" === q.toString)
  }


  test ("value toString") {
    assert ("v" === Qualifiers ("v").toString)
    val v1 = Qualifiers (List (NameVal("v", None), NameVal ("a", Some("b"))))
    assert ("v;a=b" === v1.toString)
  }


  test ("simple") {
    val h = Header ("Accept-Ranges: bytes")
    assert ("Accept-Ranges" === h.name)
    assert ("bytes" === h.value)
    assert ("Accept-Ranges: bytes" === h.toString)
    //Allow: GET, HEAD, PUT
  }


  test ("value toInt") {
    val h = Header ("Content-Length: 123")
    assert ("Content-Length" === h.name)
    assert (123 === h.toNumber.toInt)
  }


  test ("value toLong") {
    val h = Header ("Content-Length: 123")
    assert ("Content-Length" === h.name)
    assert (123 === h.toNumber.toLong)
  }


  test ("simple list") {
    val h = Header ("Allow: GET, POST, PUT")
    val v = h.toQualifiedValue
    assert ("Allow" === h.name)
    assert (3 === v.parts.size)
    assert ("GET" === v.parts (0).value)
    assert ("Allow: GET, POST, PUT" === h.toString)
    assert ("GET, POST, PUT" === v.toString)
  }


  test ("oneQ") {
    val h = Header ("Accept: audio/*;q=0.2, audio/basic")
    val v = h.toQualifiedValue
    assert ("Accept" === h.name)
    assert (2 === v.parts.size)
    assert ("audio/*" === v.parts (0).value)
    assert ("q" === v.parts (0).qualifiers (1).name)
    assert ("0.2" === v.parts (0).qualifiers (1).value.get)
    assert ("audio/basic" === v.parts (1).value)
    assert ("Accept: audio/*;q=0.2, audio/basic" === h.toString)
    assert ("audio/*;q=0.2, audio/basic" === v.toString)
  }


  test ("range") {
    val h = Header ("Accept-Ranges: bytes=500-599,700-799")
    val v = h.toRangeValue
    assert ("Accept-Ranges" === h.name)
    assert (2 === v.parts.size)
    assert ("Accept-Ranges: bytes=500-599,700-799" === h.toString)
    assert ("bytes=500-599,700-799" === v.toString)
  }


  test ("date") {
    val time = DatatypeConverter.parseDateTime ("1994-11-06T08:49:37Z").getTime
    val h = Header ("Date: Sun, 06 Nov 1994 08:49:37 GMT")
    assert ("Date" === h.name)
    assert (time === h.toDate.date.date)
  }

}
