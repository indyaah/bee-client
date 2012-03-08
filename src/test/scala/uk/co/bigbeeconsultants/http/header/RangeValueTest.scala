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

class RangeValueTest extends FunSuite {


  test ("Range construction and toString") {
    expect ("")(Range ("").toString)
    expect ("123-")(Range ("123").toString)
    expect ("123-")(Range ("123-").toString)
    expect ("-123")(Range ("-123").toString)
  }


  test ("Range isValid") {
    expect (true)(Range ("0-499").isValid)
    expect (true)(Range ("0-").isValid)
    expect (true)(Range ("100-").isValid)
    expect (true)(Range ("-499").isValid)
    expect (false)(Range ("").isValid)
    expect (false)(Range ("200-100").isValid)
  }


  test ("Range of") {
    expect ((0, 500))(Range ("0-499").of(10000))
    expect ((500, 500))(Range ("500-999").of(10000))
    expect ((9500, 500))(Range ("-500").of(10000))
    expect ((9500, 500))(Range ("9500-").of(10000))
    expect ((0, 1))(Range ("0-0").of(10000))
    expect ((9999, 1))(Range ("-1").of(10000))
  }


  test ("RangeValue 1") {
    val r = RangeValue("bytes=0-499")
    expect (true)(r.isValid)
    expect ("bytes=0-499")(r.toString)
    expect ((0, 500))(r(0).left.get.of(10000))
  }

  test ("RangeValue 2") {
    val r = RangeValue("bytes=500-599,700-799")
    expect (true)(r.isValid)
    expect ("bytes=500-599,700-799")(r.toString)
    expect ((500, 100))(r(0).left.get.of(10000))
    expect ((700, 100))(r(1).left.get.of(10000))
  }

  test ("RangeValue bytes") {
    val r = RangeValue("bytes")
    expect (true)(r.isValid)
    expect ("bytes")(r.toString)
  }

  test ("RangeValue none") {
    val r = RangeValue("none")
    expect (true)(r.isValid)
    expect ("none")(r.toString)
  }

  test ("RangeValue invalid") {
    val r = RangeValue("invalid")
    expect (false)(r.isValid)
    expect ("invalid")(r.toString)
  }
}
