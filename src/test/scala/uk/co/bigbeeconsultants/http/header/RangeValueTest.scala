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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RangeValueTest extends FunSuite {


  test ("Range construction and toString") {
    assert ("" === Range ("").toString)
    assert ("123-" === Range ("123").toString)
    assert ("123-" === Range ("123-").toString)
    assert ("-123" === Range ("-123").toString)
  }


  test ("Range isValid") {
    assert (true === Range ("0-499").isValid)
    assert (true === Range ("0-").isValid)
    assert (true === Range ("100-").isValid)
    assert (true === Range ("-499").isValid)
    assert (false === Range ("").isValid)
    assert (false === Range ("200-100").isValid)
  }


  test ("Range of") {
    assert ((0, 500) === Range ("0-499").of(10000))
    assert ((500, 500) === Range ("500-999").of(10000))
    assert ((9500, 500) === Range ("-500").of(10000))
    assert ((9500, 500) === Range ("9500-").of(10000))
    assert ((0, 1) === Range ("0-0").of(10000))
    assert ((9999, 1) === Range ("-1").of(10000))
  }


  test ("RangeValue 1") {
    val r = RangeValue("bytes=0-499")
    assert (true === r.isValid)
    assert ("bytes=0-499" === r.toString)
    assert ("bytes=0-499" === r.value)
    assert ((0, 500) === r(0).left.get.of(10000))
  }

  test ("RangeValue 2") {
    val r = RangeValue("bytes=500-599,700-799")
    assert (true === r.isValid)
    assert ("bytes=500-599,700-799" === r.toString)
    assert ((500, 100) === r(0).left.get.of(10000))
    assert ((700, 100) === r(1).left.get.of(10000))
  }

  test ("RangeValue bytes") {
    val r = RangeValue("bytes")
    assert (true === r.isValid)
    assert ("bytes" === r.toString)
    assert ("bytes" === r.value)
  }

  test ("RangeValue none") {
    val r = RangeValue("none")
    assert (true === r.isValid)
    assert ("none" === r.toString)
    assert ("none" === r.value)
  }

  test ("RangeValue invalid") {
    val r = RangeValue("invalid")
    assert (false === r.isValid)
    assert ("invalid" === r.toString)
    assert ("invalid" === r.value)
  }
}
