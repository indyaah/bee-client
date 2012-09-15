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

package uk.co.bigbeeconsultants.http.header

import org.scalatest.FunSuite

class QualifiedValueTest extends FunSuite {


  test ("NameVal with pair toString") {
    val q = NameVal ("a=b")
    expect ("a=b")(q.toString)
    expect ("a")(q.name)
    expect ("b")(q.value.get)
  }


  test ("NameVal without pair toString") {
    val q = NameVal ("aaa")
    expect ("aaa")(q.toString)
    expect ("aaa")(q.name)
    expect (None)(q.value)
  }


  test ("Qualifiers toString") {
    expect ("v")(Qualifiers ("v").toString)
    val v1 = Qualifiers (List (NameVal("v"), NameVal ("a=b")))
    expect ("v;a=b")(v1.toString)
  }


  test ("Qualifiers two-part") {
    val v = Qualifiers("audio/*; q=0.2")
    expect (2)(v.qualifiers.size)
    //expect ("audio/*")(v(0))
    expect ("audio/*")(v (0).name)
    expect ("q")(v (1).name)
    expect ("0.2")(v (1).value.get)
    expect ("q=0.2")(v(1).toString)
    expect ("audio/*;q=0.2")(v.toString)
    assert(v.isValid)
  }


  test ("two-part string ") {
    val v = QualifiedValue("audio/basic")
    expect (1)(v.parts.size)
    expect ("audio/basic")(v(0).toString)
    expect ("audio/basic")(v.parts(0).value)
    expect ("audio/basic")(v.toString)
    assert(v.isValid)
  }


  test ("two-part string with one qualifier including spaces and zeros") {
    val v = QualifiedValue("audio/*; q=0.2, audio/basic")
    expect (2)(v.parts.size)
    //expect ("audio/*")(v(0))
    expect ("audio/*")(v.parts (0).value)
    expect ("audio/*")(v(0).value)
    expect ("q")(v.parts (0).qualifiers (1).name)
    expect ("q")(v(0)(1).name)
    expect ("0.2")(v.parts (0).qualifiers (1).value.get)
    expect ("0.2")(v(0)(1).value.get)
    expect ("audio/basic")(v(1).toString)
    expect ("audio/*;q=0.2, audio/basic")(v.toString)
    assert(v.isValid)
  }


  test ("two-part string with one qualifier without spaces or zeros") {
    val v = QualifiedValue("audio/*;q=.2, audio/basic")
    expect (2)(v.parts.size)
    //expect ("audio/*")(v(0))
    expect ("audio/*")(v(0).value)
    expect ("q")(v(0)(1).name)
    expect (".2")(v(0)(1).value.get)
    expect ("audio/basic")(v(1).toString)
    expect ("audio/*;q=.2, audio/basic")(v.toString)
    assert(v.isValid)
  }


  test ("complex string") {
    val v = QualifiedValue ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")
    expect (5)(v.parts.size)
    expect ("text/*")(v(0).value)
    expect ("q")(v(0)(1).name)
    expect ("0.3")(v(0)(1).value.get)
    expect ("text/html")(v(3).value)
    expect ("level")(v(3)(1).name)
    expect ("2")(v(3)(1).value.get)
    expect ("q")(v(3)(2).name)
    expect ("0.4")(v(3)(2).value.get)
    expect ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")(v.toString)
    assert(v.isValid)
  }


  test ("invalid strings") {
//    assert(!QualifiedValue ("q=0.3").isValid)
    assert(!QualifiedValue (";q=0.3").isValid)
  }

}
