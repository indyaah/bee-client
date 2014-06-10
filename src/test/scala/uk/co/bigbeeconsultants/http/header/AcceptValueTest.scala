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
class AcceptValueTest extends FunSuite {

  test("one media type") {
    val v = AcceptValue(MediaType.TEXT_HTML)
    assert(v.value === "text/html")
    assert(v.toString === "text/html")
    assert(v.isValid)
  }

  test("digit pattern") {
    assert(AcceptValue.QualityPattern.matcher("1").matches)
    assert(AcceptValue.QualityPattern.matcher("1.000").matches)
    assert(!AcceptValue.QualityPattern.matcher("1.0000").matches)
    assert(AcceptValue.QualityPattern.matcher("0.9").matches)
    assert(AcceptValue.QualityPattern.matcher("0.123").matches)
    assert(!AcceptValue.QualityPattern.matcher("0.1234").matches)
    assert(AcceptValue.QualityPattern.matcher("0").matches)
    assert(!AcceptValue.QualityPattern.matcher("2").matches)
    assert(!AcceptValue.QualityPattern.matcher("0.x").matches)
  }

  test("concatenation") {
    val v1 = AcceptValue(MediaType.TEXT_HTML)

    val v2 = v1 + MediaType.TEXT_CSS
    assert(v2.value === "text/html, text/css")
    assert(v2.toString === "text/html, text/css")
    assert(v2.isValid)

    val v3 = v2.append(MediaType.TEXT_CSV, 0.2f)
    assert(v3.value === "text/html, text/css, text/csv;q=0.2")
    assert(v3.isValid)
  }

}
