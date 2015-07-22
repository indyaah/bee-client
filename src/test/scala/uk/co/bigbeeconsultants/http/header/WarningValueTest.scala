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
class WarningValueTest extends FunSuite {

  test ("WarningValue parse happy without date") {
    val w = WarningValue("""110 somehost:80 "stale"""")
    assert (true === w.isValid)
    assert (110 === w.code)
    assert ("somehost:80" === w.agent)
    assert ("stale" === w.text)
    assert ("""110 somehost:80 "stale"""" === w.toString)
  }

  test ("WarningValue parse happy with date") {
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

  test ("WarningValue parse invalid 1") {
    val w = WarningValue("")
    assert (false === w.isValid)
    assert ("" === w.toString)
    assert ("" === w.text)
  }

  test ("WarningValue parse invalid 2") {
    val w = WarningValue("not a number")
    assert (false === w.isValid)
    assert ("not a number" === w.toString)
    assert ("" === w.text)
  }

  test ("WarningValue parse invalid 3") {
    val w = WarningValue("     ")
    assert (false === w.isValid)
    assert ("     " === w.toString)
    assert ("" === w.text)
  }

  test ("WarningValue parse invalid 4") {
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

  test ("WarningValue apply happy with date") {
    val d = new HttpDateTimeInstant()
    val w = WarningValue(110, "myhost", "stale", Some(d))
    assert ("110 myhost \"stale\" \"" + d + '"' === w.value)
    assert (true === w.isValid)
    assert (110 === w.code)
    assert ("myhost" === w.agent)
    assert ("stale" === w.text)
    assert (Some(d) === w.date)
  }

  test ("WarningValue apply happy without date") {
    val w = WarningValue(110, "myhost", "stale", None)
    assert ("110 myhost \"stale\"" === w.value)
    assert (true === w.isValid)
    assert (110 === w.code)
    assert ("myhost" === w.agent)
    assert ("stale" === w.text)
    assert (None === w.date)
  }

  test ("WarningListValue parse happy with date") {
    val d = new HttpDateTimeInstant()
    val v1 = """110 pseudonym1 "stale1""""
    val v2 = """111 pseudonym2 "stale2" """" + d + '"'
    val wl = WarningListValue(" " + v1 + " , " + v2 + " ")
    assert (wl.toString === v1 + ", " + v2)
    assert (wl.isValid)
    assert (wl.size === 2)
    assert (wl(0).isValid)
    assert (wl(1).isValid)
    assert (wl(0).code === 110)
    assert (wl(1).code === 111)
    assert (wl(0).agent === "pseudonym1")
    assert (wl(1).agent === "pseudonym2")
    assert (wl(0).text === "stale1")
    assert (wl(1).text === "stale2")
    assert (wl(0).date === None)
    assert (wl(1).date === Some(d))
  }

}
