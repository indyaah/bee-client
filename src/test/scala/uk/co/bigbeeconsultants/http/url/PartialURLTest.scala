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
    expect("http")(purl.scheme.get)
    expect("www.w3.org")(purl.host.get)
    expect(None)(purl.port)
    expect(false)(purl.path.isAbsolute)
    expect(Nil)(purl.path.segments)
    expect(None)(purl.fragment)
    expect(None)(purl.query)
    expect(None)(purl.file)
    expect(None)(purl.extension)
    expect(s)(purl.toString)
    expect(u)(purl.asURL)
    expect(purl)(PartialURL(u))
  }

  test("absolute url with short path") {
    val s = "http://www.w3.org/"
    val u = new URL(s)
    val purl = PartialURL(s)
    expect("http")(purl.scheme.get)
    expect("www.w3.org")(purl.host.get)
    expect(None)(purl.port)
    expect(true)(purl.path.isAbsolute)
    expect(Nil)(purl.path.segments)
    expect(None)(purl.fragment)
    expect(None)(purl.query)
    expect(None)(purl.file)
    expect(None)(purl.extension)
    expect(s)(purl.toString)
    expect(u)(purl.asURL)
    expect(purl)(PartialURL(u))
  }

  test("absolute url with path only") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html"
    val u = new URL(s)
    val purl = PartialURL(s)
    expect("http")(purl.scheme.get)
    expect("www.w3.org")(purl.host.get)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect(None)(purl.fragment)
    expect(None)(purl.query)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(u)(purl.asURL)
    expect(purl)(PartialURL(u))
  }

  test("absolute url with fragment") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html#z12"
    val u = new URL(s)
    val purl = PartialURL(s)
    expect("http")(purl.scheme.get)
    expect("www.w3.org")(purl.host.get)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect("z12")(purl.fragment.get)
    expect(None)(purl.query)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(u)(purl.asURL)
    expect(purl)(PartialURL(u))
  }

  test("absolute url with port and query") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html?red=yes"
    val u = new URL(s)
    val purl = PartialURL(s)
    expect("http")(purl.scheme.get)
    expect("myserver")(purl.host.get)
    expect(8080)(purl.port.get)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect(None)(purl.fragment)
    expect("red=yes")(purl.query.get)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(u)(purl.asURL)
    expect(purl)(PartialURL(u))
  }

  test("absolute url with port, fragment and query") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html#there?red=yes"
    val u = new URL(s)
    val purl = PartialURL(s)
    expect("http")(purl.scheme.get)
    expect("myserver")(purl.host.get)
    expect(8080)(purl.port.get)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect("there")(purl.fragment.get)
    expect("red=yes")(purl.query.get)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(u)(purl.asURL)
    expect(purl)(PartialURL(u))
  }

  test("relative url without any path") {
    val s = ""
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(false)(purl.path.isAbsolute)
    expect(Nil)(purl.path.segments)
    expect(None)(purl.fragment)
    expect(None)(purl.query)
    expect(None)(purl.file)
    expect(None)(purl.extension)
    expect(s)(purl.toString)
    expect(false)(purl.isURL)
  }

  test("relative url with short path") {
    val s = "/"
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(true)(purl.path.isAbsolute)
    expect(Nil)(purl.path.segments)
    expect(None)(purl.fragment)
    expect(None)(purl.query)
    expect(None)(purl.file)
    expect(None)(purl.extension)
    expect(s)(purl.toString)
    expect(false)(purl.isURL)
  }

  test("relative url with path only") {
    val s = "/Addressing/URL/5_BNF.html"
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect(None)(purl.fragment)
    expect(None)(purl.query)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(false)(purl.isURL)
  }

  test("relative url with fragment") {
    val s = "/Addressing/URL/5_BNF.html#z12"
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect("z12")(purl.fragment.get)
    expect(None)(purl.query)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(false)(purl.isURL)
  }

  test("relative url with port and query") {
    val s = "/Addressing/URL/5_BNF.html?red=yes"
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect(None)(purl.fragment)
    expect("red=yes")(purl.query.get)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(false)(purl.isURL)
  }

  test("relative url with port, fragment and query") {
    val s = "/Addressing/URL/5_BNF.html#there?red=yes"
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect("there")(purl.fragment.get)
    expect("red=yes")(purl.query.get)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect(s)(purl.toString)
    expect(false)(purl.isURL)
  }

  test("relative url with port, query and fragment") {
    val s = "/Addressing/URL/5_BNF.html?red=yes#there"
    val purl = PartialURL(s)
    expect(None)(purl.scheme)
    expect(None)(purl.host)
    expect(None)(purl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(purl.path.segments)
    expect("there")(purl.fragment.get)
    expect("red=yes")(purl.query.get)
    expect("5_BNF.html")(purl.file.get)
    expect("html")(purl.extension.get)
    expect("/Addressing/URL/5_BNF.html#there?red=yes")(purl.toString)
    expect(false)(purl.isURL)
  }

}
