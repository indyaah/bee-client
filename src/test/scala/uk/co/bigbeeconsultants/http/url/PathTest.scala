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

  test("parse and equals") {
    assert(Path(false, Nil) === Path.empty)
//    assert(Path(true, Nil) === Path.slash)
    assert(Path(false, Nil) === Path(""))
    assert(Path(true, Nil) === Path("/"))
    assert(Path(true, List("a")) === Path("/a"))
    assert(Path(false, List("a")) === Path("a"))
    assert(Path(true, List("a", "b")) === Path("/a/b"))
    assert(Path(false, List("a", "b")) === Path("a/b"))
  }

  test("toString") {
    assert("" === Path.empty.toString)
    assert("" === Path("").toString)
    assert("/" === Path("/").toString)
    assert("a" === Path("a").toString)
    assert("/a/b" === Path("/a/b").toString)
    assert("a/b" === Path("a/b").toString)
  }

  val abcdef = List("a", "b", "c", "d", "e", "f")

  test("+ concatenation") {
    assert(Path(true, abcdef) === Path("/a/b/c") + Path("d/e/f"))
  }

  test("/ concatenation") {
    assert(Path(true, abcdef) === Path("/a/b/c") / "d" / "e" / "f")
  }

  test("init") {
    assert(Path(true, abcdef.init) === Path("/a/b/c/d/e/f").init)
    assert(Path(false, abcdef.init) === Path("a/b/c/d/e/f").init)
  }

  test("tail") {
    assert(Path(false, abcdef.tail) === Path("/a/b/c/d/e/f").tail)
    assert(Path(false, abcdef.tail) === Path("a/b/c/d/e/f").tail)
  }

  test("drop") {
    assert(Path(false, abcdef.drop(3)) === Path("/a/b/c/d/e/f").drop(3))
    assert(Path(false, abcdef.drop(3)) === Path("a/b/c/d/e/f").drop(3))
  }

  test("take") {
    assert(Path(true, abcdef.take(3)) === Path("/a/b/c/d/e/f").take(3))
    assert(Path(false, abcdef.take(3)) === Path("a/b/c/d/e/f").take(3))
  }

  test("startsWith") {
    assert(Path("/a/b/c/d/e/f").startsWith(Path("/")))
    assert(Path("/a/b/c/d/e/f").startsWith(Path("/a/b")))
    assert(Path("a/b/c/d/e/f").startsWith(Path("a/b")))
    assert(!Path("a/b/c/d/e/f").startsWith(Path("/a/b")))
    assert(!Path("/a/b/c/d/e/f").startsWith(Path("a/b")))
    assert(Path("/a/b/c/d/e/f").startsWith(Path("/a/b/c/d/e/f")))
    assert(!Path("/a/b/c/d/e/f").startsWith(Path("/a/b/c/d/e/f/g")))
    assert(!Path("").startsWith(Path("/a/b/c/d/e/f/g")))
    assert(!Path("").startsWith(Path("/")))
    assert(!Path("/").startsWith(Path("")))
    assert(Path("").startsWith(Path("")))
  }
}
