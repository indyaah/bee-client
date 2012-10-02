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

import java.net.URL
import org.scalatest.FunSuite

class PartialURLTest extends FunSuite {

  test("absolute url without any path") {
    val s = "http://www.w3.org"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert("http" === purl.scheme.get)
    assert("www.w3.org" === purl.host.get)
    assert(None === purl.port)
    assert(false === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
  }

  test("absolute url with short path") {
    val s = "http://www.w3.org/"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert("http" === purl.scheme.get)
    assert("www.w3.org" === purl.host.get)
    assert(None === purl.port)
    assert(true === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
  }

  test("absolute url with path only") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert("http" === purl.scheme.get)
    assert("www.w3.org" === purl.host.get)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
  }

  test("absolute url with fragment") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html#z12"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert("http" === purl.scheme.get)
    assert("www.w3.org" === purl.host.get)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert("z12" === purl.fragment.get)
    assert(None === purl.query)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
  }

  test("absolute url with port and query") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html?red=yes"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert("http" === purl.scheme.get)
    assert("myserver" === purl.host.get)
    assert(8080 === purl.port.get)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert(None === purl.fragment)
    assert("red=yes" === purl.query.get)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
  }

  test("absolute url with port, fragment and query") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html#there?red=yes"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert("http" === purl.scheme.get)
    assert("myserver" === purl.host.get)
    assert(8080 === purl.port.get)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert("there" === purl.fragment.get)
    assert("red=yes" === purl.query.get)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
  }

  test("relative url without any path") {
    val s = ""
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(false === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(false === purl.isURL)
  }

  test("relative url with short path") {
    val s = "/"
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(true === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(false === purl.isURL)
  }

  test("relative url with path only") {
    val s = "/Addressing/URL/5_BNF.html"
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(false === purl.isURL)
  }

  test("relative url with fragment") {
    val s = "/Addressing/URL/5_BNF.html#z12"
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert("z12" === purl.fragment.get)
    assert(None === purl.query)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(false === purl.isURL)
  }

  test("relative url with port and query") {
    val s = "/Addressing/URL/5_BNF.html?red=yes"
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert(None === purl.fragment)
    assert("red=yes" === purl.query.get)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(false === purl.isURL)
  }

  test("relative url with port, fragment and query") {
    val s = "/Addressing/URL/5_BNF.html#there?red=yes"
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert("there" === purl.fragment.get)
    assert("red=yes" === purl.query.get)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(false === purl.isURL)
  }

  test("relative url with port, query and fragment") {
    val s = "/Addressing/URL/5_BNF.html?red=yes#there"
    val purl = PartialURL(s)
    assert(None === purl.scheme)
    assert(None === purl.host)
    assert(None === purl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert("there" === purl.fragment.get)
    assert("red=yes" === purl.query.get)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert("/Addressing/URL/5_BNF.html#there?red=yes" === purl.toString)
    assert(false === purl.isURL)
  }

}
