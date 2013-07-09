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
import java.io.{InputStreamReader, BufferedReader, ByteArrayInputStream, ByteArrayOutputStream}

class LineSplitterTest extends FunSuite {

  private def constructReader(s: String) = {
    val bytes = s.getBytes("ASCII")
    val bais = new ByteArrayInputStream(bytes)
    new BufferedReader(new InputStreamReader(bais))
  }

  private def dumbIterator(s: String) = new Iterator[String] {
    val r = constructReader(s)
    var x = r.readLine()

    def hasNext = x != null

    def next(): String = {
      val n = x
      x = r.readLine()
      n
    }
  }

  test("blank string") {
    val splitter = new LineSplitter("")
    assert(!splitter.hasNext)
  }

  test("reference-blank string") {
    val splitter = dumbIterator("")
    assert(!splitter.hasNext)
  }

  test("one line") {
    val splitter = new LineSplitter("one line of text")
    assert(splitter.hasNext)
    assert(splitter.next === "one line of text")
    assert(!splitter.hasNext)
  }

  test("reference-one line") {
    val splitter = dumbIterator("one line of text")
    assert(splitter.hasNext)
    assert(splitter.next === "one line of text")
    assert(!splitter.hasNext)
  }

  test("several lines, using newline") {
    val splitter = new LineSplitter("line 1\nline 2\n\nline 4")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("reference-several lines, using newline") {
    val splitter = dumbIterator("line 1\nline 2\n\nline 4")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("several lines, using cr/nl") {
    val splitter = new LineSplitter("line 1\r\nline 2\r\n\r\nline 4")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("reference-several lines, using cr/nl") {
    val splitter = dumbIterator("line 1\r\nline 2\r\n\r\nline 4")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("several lines with extremities, using newline") {
    val splitter = new LineSplitter("\nline 1\nline 2\n\nline 4\n")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("reference-several lines with extremities, using newline") {
    val splitter = dumbIterator("\nline 1\nline 2\n\nline 4\n")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("several lines with extremities, using cr/nl") {
    val splitter = new LineSplitter("\r\nline 1\r\nline 2\r\n\r\nline 4\r\n")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }

  test("reference-several lines with extremities, using cr/nl") {
    val splitter = dumbIterator("\r\nline 1\r\nline 2\r\n\r\nline 4\r\n")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 1")
    assert(splitter.hasNext)
    assert(splitter.next === "line 2")
    assert(splitter.hasNext)
    assert(splitter.next === "")
    assert(splitter.hasNext)
    assert(splitter.next === "line 4")
    assert(!splitter.hasNext)
  }
}
