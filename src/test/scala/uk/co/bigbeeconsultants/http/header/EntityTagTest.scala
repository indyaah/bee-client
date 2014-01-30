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

class EntityTagTest extends FunSuite {

  test ("EntityTag parse happy not weak") {
    val w = EntityTag(""""abcdef"""")
    assert (true === w.isValid)
    assert (false === w.isWeak)
    assert ("abcdef" === w.opaqueTag)
    assert (""""abcdef"""" === w.toString)
  }

  test ("EntityTag parse happy weak") {
    val w = EntityTag("""W/"abcdef"""")
    assert (true === w.isValid)
    assert (true === w.isWeak)
    assert ("abcdef" === w.opaqueTag)
    assert ("""W/"abcdef"""" === w.toString)
  }

  test ("EntityTag constructor 1") {
    val w = EntityTag("aaa", true)
    assert (true === w.isValid)
    assert (true === w.isWeak)
    assert ("aaa" === w.opaqueTag)
    assert ("""W/"aaa"""" === w.toString)
  }

  test ("EntityTag constructor 2") {
    val w = EntityTag("aaa", false)
    assert (true === w.isValid)
    assert (false === w.isWeak)
    assert ("aaa" === w.opaqueTag)
    assert (""""aaa"""" === w.toString)
  }

  test ("EntityTagListValue apply 1") {
    val tl = EntityTagListValue(""""abcdef", "ghijkl"""")
    assert (tl.size === 2)
    assert (tl(0).isValid === true)
    assert (tl(1).isValid === true)
    assert (tl.isValid === true)
    assert (tl(0).isWeak === false)
    assert (tl(1).isWeak === false)
    assert (tl(0).opaqueTag === "abcdef")
    assert (tl(1).opaqueTag === "ghijkl")
  }

  test ("EntityTagListValue apply 2") {
    val tl = EntityTagListValue(""""abcdef", W/"ghijkl"""")
    assert (tl.size === 2)
    assert (tl(0).isValid === true)
    assert (tl(1).isValid === true)
    assert (tl.isValid === true)
    assert (tl(0).isWeak === false)
    assert (tl(1).isWeak === true)
    assert (tl(0).opaqueTag === "abcdef")
    assert (tl(1).opaqueTag === "ghijkl")
  }

}
