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

package uk.co.bigbeeconsultants.http.util

import org.scalatest.FunSuite

class SplitterTest extends FunSuite {

  test("blank string") {
    val splitter = new Splitter("", '\n')
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(!splitter.hasNext)
  }

  test("one line") {
    val splitter = new Splitter("one line of text", '\n')
    assert(splitter.hasNext)
    assert(splitter.next === "one line of text")
    assert(!splitter.hasNext)
  }

  test("several lines") {
    val splitter = new Splitter("line 1\nline 2\nline 3", '\n')
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "line 3")
    assert(!splitter.hasNext)
  }

  test("several lines with extremities") {
    val splitter = new Splitter("\nline 1\nline 2\nline 3\n", '\n')
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "line 3")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(!splitter.hasNext)
  }
}
