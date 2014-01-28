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

class CacheControlValueTest extends FunSuite {

  test("faulty construction with blank string") {
    val ccv = CacheControlValue("")
    assert(!ccv.isValid)
    assert(ccv.label === "")
    assert(ccv.deltaSeconds === None)
    assert(ccv.fieldName === None)
    assert(ccv.value === "")
  }

  test("construction with some field name") {
    val value = """no-cache="thefield""""
    val ccv = CacheControlValue(value)
    assert(ccv.isValid)
    assert(ccv.label === "no-cache")
    assert(ccv.deltaSeconds === None)
    assert(ccv.fieldName === Some("thefield"))
    assert(ccv.value === value)
  }

  test("construction without delta") {
    val ccv = CacheControlValue("no-cache")
    assert(ccv.isValid)
    assert(ccv.label === "no-cache")
    assert(ccv.deltaSeconds === None)
    assert(ccv.fieldName === None)
    assert(ccv.value === "no-cache")
  }

  test("construction with delta") {
    val ccv = CacheControlValue("max-age=123")
    assert(ccv.isValid)
    assert(ccv.label === "max-age")
    assert(ccv.deltaSeconds === Some(123))
    assert(ccv.fieldName === None)
    assert(ccv.value === "max-age=123")
  }

  test("construction with invalid delta") {
    val ccv = CacheControlValue("max-age=none")
    assert(!ccv.isValid)
    assert(ccv.label === "max-age")
    assert(ccv.deltaSeconds === None)
    assert(ccv.fieldName === None)
    assert(ccv.value === "max-age=none")
  }
}
