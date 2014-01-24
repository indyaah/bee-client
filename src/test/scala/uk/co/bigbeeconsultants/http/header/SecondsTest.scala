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
import java.util.Date

class SecondsTest extends FunSuite {

  test("plus long") {
    val now = Seconds.sinceEpoch
    val later = now + 60
    assert(60 === (later.seconds - now.seconds))
  }

  test("minus long") {
    val now = Seconds.sinceEpoch
    val later = now - 60
    assert(-60 === (later.seconds - now.seconds))
  }

  test("plus Seconds") {
    val now = Seconds.sinceEpoch
    val later = now + Seconds(60)
    assert(60 === (later.seconds - now.seconds))
  }

  test("minus Seconds") {
    val now = Seconds.sinceEpoch
    val later = now - Seconds(60)
    assert(-60 === (later.seconds - now.seconds))
  }

  test("times long") {
    val now = Seconds.sinceEpoch
    val now5 = now * 5
    assert(now5.seconds === now.seconds * 5)
  }

  test("divide long") {
    val now = Seconds.sinceEpoch
    val now5 = now / 5
    assert(now5.seconds === now.seconds / 5)
  }

  test("milliseconds") {
    val now = System.currentTimeMillis() / 1000
    val s = Seconds(now)
    assert(s.milliseconds === now * 1000)
  }

  test("date") {
    val now = new Date()
    val s = Seconds(now)
    assert(s.seconds === now.getTime / 1000)
  }

  test("compare") {
    val now = Seconds.sinceEpoch
    val later = now + 60
    assert(true === (later > now))
  }

  test("max") {
    val a = Seconds(123)
    val b = Seconds(234)
    assert((a max b) === b)
    assert((b max a) === b)
    assert((b max b) === b)
    assert((a max a) === a)
  }
}
