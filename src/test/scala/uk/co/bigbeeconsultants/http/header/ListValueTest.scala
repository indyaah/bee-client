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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ListValueTest extends FunSuite {

  test("CommaListValue with three items") {
    val vu = CommaListValue.split("Gzip, Deflate, Sdhc")
    val vl = vu.toLowerCase
    assert(3 === vu.parts.size)
    assert(3 === vl.parts.size)
    assert("Gzip" === vu(0))
    assert("gzip" === vl(0))
    assert("Gzip" === vu.parts(0))
    assert("gzip" === vl.parts(0))
    assert("Gzip, Deflate, Sdhc" === vu.toString)
    assert("gzip, deflate, sdhc" === vl.toString)
  }

  test("SemicolonListValue with three items") {
    val vu = SemicolonListValue.split("X=1; Y=2; Z=3")
    val vl = vu.toLowerCase
    assert(3 === vu.parts.size)
    assert(3 === vl.parts.size)
    assert("X=1" === vu(0))
    assert("x=1" === vl(0))
    assert("X=1" === vu.parts(0))
    assert("x=1" === vl.parts(0))
    assert("X=1; Y=2; Z=3" === vu.toString)
    assert("x=1; y=2; z=3" === vl.toString)
  }

  test("NameValListValue with three items") {
    val vu = NameValListValue.split("X=A, Y=2, Z=3")
    val vl = vu.toLowerCase
    assert(vu.isValid)
    assert(vl.isValid)
    assert(vu.parts.size === 3)
    assert(vl.parts.size === 3)
    assert(vu(0).name === "X")
    assert(vl(0).name === "x")
    assert(vu(0).value === Some("A"))
    assert(vl(0).value === Some("a"))
    assert(vu.toString === "X=A, Y=2, Z=3")
    assert(vl.toString === "x=a, y=2, z=3")
  }

}
