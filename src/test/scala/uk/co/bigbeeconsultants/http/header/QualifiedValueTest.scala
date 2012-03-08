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

import org.scalatest.FunSuite

class QualifiedValueTest extends FunSuite {


  test ("Qualifier with pair toString") {
    val q = Qualifier ("a=b")
    expect ("a=b")(q.toString)
  }


  test ("Qualifier without pair toString") {
    val q = Qualifier ("aaa")
    expect ("aaa")(q.toString)
  }


  test ("QualifiedPart toString") {
    expect ("v")(QualifiedPart ("v").toString)
    val v1 = QualifiedPart ("v", List (Qualifier ("a", "b")))
    expect ("v;a=b")(v1.toString)
  }


  test ("two-part string with one qualifier") {
    val v = QualifiedValue("audio/*;q=0.2, audio/basic")
    expect (2)(v.parts.size)
    expect ("audio/*")(v(0))
    expect ("audio/*")(v.parts (0).value)
    expect ("q")(v.parts (0).qualifier (0).label)
    expect ("0.2")(v.parts (0).qualifier (0).value)
    expect ("audio/basic")(v(1))
    expect ("audio/*;q=0.2, audio/basic")(v.toString)
  }


  test ("complex string") {
    val v = QualifiedValue ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")
    expect (5)(v.parts.size)
    expect ("text/*")(v.parts (0).value)
    expect ("q")(v.parts (0).qualifier (0).label)
    expect ("0.3")(v.parts (0).qualifier (0).value)
    expect ("text/html")(v.parts (3).value)
    expect ("level")(v.parts (3).qualifier (0).label)
    expect ("2")(v.parts (3).qualifier (0).value)
    expect ("q")(v.parts (3).qualifier (1).label)
    expect ("0.4")(v.parts (3).qualifier (1).value)
    expect ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")(v.toString)
  }

}
