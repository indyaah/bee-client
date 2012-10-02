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
    assert ("a=b" === q.toString)
    assert ("a" === q.name)
    assert ("b" === q.value.get)
  }


  test ("NameVal without pair toString") {
    val q = NameVal ("aaa")
    assert ("aaa" === q.toString)
    assert ("aaa" === q.name)
    assert (None === q.value)
  }


  test ("Qualifiers toString") {
    assert ("v" === Qualifiers ("v").toString)
    val v1 = Qualifiers (List (NameVal("v"), NameVal ("a=b")))
    assert ("v;a=b" === v1.toString)
  }


  test ("Qualifiers two-part") {
    val v = Qualifiers("audio/*; q=0.2")
    assert (2 === v.qualifiers.size)
    //assert ("audio/*" === v(0))
    assert ("audio/*" === v (0).name)
    assert ("q" === v (1).name)
    assert ("0.2" === v (1).value.get)
    assert ("q=0.2" === v(1).toString)
    assert ("audio/*;q=0.2" === v.toString)
    assert(v.isValid)
  }


  test ("two-part string ") {
    val v = QualifiedValue("audio/basic")
    assert (1 === v.parts.size)
    assert ("audio/basic" === v(0).toString)
    assert ("audio/basic" === v.parts(0).value)
    assert ("audio/basic" === v.toString)
    assert(v.isValid)
  }


  test ("two-part string with one qualifier including spaces and zeros") {
    val v = QualifiedValue("audio/*; q=0.2, audio/basic")
    assert (2 === v.parts.size)
    //assert ("audio/*" === v(0))
    assert ("audio/*" === v.parts (0).value)
    assert ("audio/*" === v(0).value)
    assert ("q" === v.parts (0).qualifiers (1).name)
    assert ("q" === v(0)(1).name)
    assert ("0.2" === v.parts (0).qualifiers (1).value.get)
    assert ("0.2" === v(0)(1).value.get)
    assert ("audio/basic" === v(1).toString)
    assert ("audio/*;q=0.2, audio/basic" === v.toString)
    assert(v.isValid)
  }


  test ("two-part string with one qualifier without spaces or zeros") {
    val v = QualifiedValue("audio/*;q=.2, audio/basic")
    assert (2 === v.parts.size)
    //assert ("audio/*" === v(0))
    assert ("audio/*" === v(0).value)
    assert ("q" === v(0)(1).name)
    assert (".2" === v(0)(1).value.get)
    assert ("audio/basic" === v(1).toString)
    assert ("audio/*;q=.2, audio/basic" === v.toString)
    assert(v.isValid)
  }


  test ("complex string") {
    val v = QualifiedValue ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")
    assert (5 === v.parts.size)
    assert ("text/*" === v(0).value)
    assert ("q" === v(0)(1).name)
    assert ("0.3" === v(0)(1).value.get)
    assert ("text/html" === v(3).value)
    assert ("level" === v(3)(1).name)
    assert ("2" === v(3)(1).value.get)
    assert ("q" === v(3)(2).name)
    assert ("0.4" === v(3)(2).value.get)
    assert ("text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5" === v.toString)
    assert(v.isValid)
  }


  test ("invalid strings") {
//    assert(!QualifiedValue ("q=0.3").isValid)
    assert(!QualifiedValue (";q=0.3").isValid)
  }

}
