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

class WarningValueTest extends FunSuite {

  test ("WarningValue invalid 1") {
    val w = WarningValue("")
    assert (false === w.isValid)
    assert ("" === w.toString)
    assert ("" === w.text)
  }

  test ("WarningValue invalid 2") {
    val w = WarningValue("not a number")
    assert (false === w.isValid)
    assert ("not a number" === w.toString)
    assert ("" === w.text)
  }

  test ("WarningValue invalid 3") {
    val w = WarningValue("     ")
    assert (false === w.isValid)
    assert ("     " === w.toString)
    assert ("" === w.text)
  }

  test ("WarningValue happy 1") {
    val w = WarningValue("""110 somehost:80 "stale"""")
    assert (true === w.isValid)
    assert (110 === w.code)
    assert ("somehost:80" === w.agent)
    assert ("stale" === w.text)
    assert ("""110 somehost:80 "stale"""" === w.toString)
  }

  test ("WarningValue happy 2") {
    val d = new HttpDateTimeInstant()
    val v = """110 pseudonym "stale" """" + d + '"'
    val w = WarningValue(v)
    assert (v === w.toString)
    assert (true === w.isValid)
    assert (110 === w.code)
    assert ("pseudonym" === w.agent)
    assert ("stale" === w.text)
    assert (Some(d) === w.date)
  }

  test ("WarningValue invalid 4") {
    val d = new HttpDateTimeInstant()
    val v = """110 pseudonym "stale" """ + d
    val w = WarningValue(v)
    assert (v === w.toString)
    assert (false === w.isValid)
    assert (0 === w.code)
    assert ("" === w.agent)
    assert ("" === w.text)
    assert (None === w.date)
  }
}
