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

package uk.co.bigbeeconsultants.http.url

import org.scalatest.FunSuite

class PathTest extends FunSuite {

  test("parse") {
    expect(Path(false, Nil))(Path.empty)
    expect(Path(false, Nil))(Path(""))
    expect(Path(true, Nil))(Path("/"))
    expect(Path(true, List("a")))(Path("/a"))
    expect(Path(true, List("a")))(Path("a"))
    expect(Path(true, List("a", "b")))(Path("/a/b"))
    expect(Path(true, List("a", "b")))(Path("a/b"))
  }

  test("toString") {
    expect("")(Path.empty.toString)
    expect("")(Path("").toString)
    expect("/")(Path("/").toString)
    expect("a")(Path("a").toString)
    expect("/a/b")(Path("/a/b").toString)
    expect("a/b")(Path("a/b").toString)
  }
}
