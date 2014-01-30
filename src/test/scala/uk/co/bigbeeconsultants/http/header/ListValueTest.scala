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

class ListValueTest extends FunSuite {

  test("CommaListValue with three items") {
    val v = CommaListValue.split("gzip, deflate, sdhc")
    assert(3 === v.parts.size)
    assert("gzip" === v(0))
    assert("gzip" === v.parts(0))
    assert("gzip, deflate, sdhc" === v.toString)
  }

  test("SemicolonListValue with three items") {
    val v = SemicolonListValue.split("x=1; y=2; z=3")
    assert(3 === v.parts.size)
    assert("x=1" === v(0))
    assert("x=1" === v.parts(0))
    assert("x=1; y=2; z=3" === v.toString)
  }

  test("NameValListValue with three items") {
    val v = NameValListValue.split("x=1, y=2, z=3")
    assert(v.isValid)
    assert(v.parts.size === 3)
    assert(v(0).name === "x")
    assert(v(0).value === Some("1"))
    assert(v.toString === "x=1, y=2, z=3")
  }

}
