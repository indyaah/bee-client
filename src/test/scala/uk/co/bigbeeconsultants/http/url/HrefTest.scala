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

class HrefTest extends FunSuite {

  val w3Org = "http://www.w3.org"
  val myserver8080 = "http://myserver:8080"

  test("absolute url without any path") {
    val u = new URL(w3Org)
    val href = Href(w3Org)
    assert(w3Org === href.endpoint.get.toString)
    assert(Nil === href.path.segments)
    assert(None === href.fragment)
    assert(None === href.query)
    assert(None === href.file)
    assert(None === href.extension)
    assert(w3Org + "/" === href.toString)
    assert(new URL(w3Org + "/") === href.asURL)
    assert(href === Href(u))
    assert(href.path.isAbsolute)
  }

  test("absolute url with short path") {
    val s = "http://www.w3.org/"
    val u = new URL(s)
    val href = Href(s)
    assert(w3Org === href.endpoint.get.toString)
    assert(true === href.path.isAbsolute)
    assert(Nil === href.path.segments)
    assert(None === href.fragment)
    assert(None === href.query)
    assert(None === href.file)
    assert(None === href.extension)
    assert(s === href.toString)
    assert(u === href.asURL)
    assert(href === Href(u))
    assert(href.path.isAbsolute)
  }

  test("absolute url with path only") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html"
    val u = new URL(s)
    val href = Href(s)
    assert(w3Org === href.endpoint.get.toString)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert(None === href.fragment)
    assert(None === href.query)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(u === href.asURL)
    assert(href === Href(u))
    assert(href.startsWith(Href(w3Org)))
  }

  test("absolute url with fragment") {
    val s = w3Org + "/Addressing/URL/5_BNF.html#z12"
    val u = new URL(s)
    val href = Href(s)
    assert(w3Org === href.endpoint.get.toString)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert("z12" === href.fragment.get)
    assert(None === href.query)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(u === href.asURL)
    assert(href === Href(u))
  }

  test("absolute url with port and query") {
    val s = myserver8080 + "/Addressing/URL/5_BNF.html?red=yes"
    val u = new URL(s)
    val href = Href(s)
    assert(myserver8080 === href.endpoint.get.toString)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert(None === href.fragment)
    assert("red=yes" === href.query.get)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(u === href.asURL)
    assert(href === Href(u))
  }

  test("absolute url with port, fragment and query") {
    val s = myserver8080 + "/Addressing/URL/5_BNF.html#there?red=yes"
    val u = new URL(s)
    val href = Href(s)
    assert(myserver8080 === href.endpoint.get.toString)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert("there" === href.fragment.get)
    assert("red=yes" === href.query.get)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(u === href.asURL)
    assert(href === Href(u))
  }

  test("relative url without any path") {
    val s = ""
    val href = Href(s)
    assert(None === href.endpoint)
    assert(false === href.path.isAbsolute)
    assert(Nil === href.path.segments)
    assert(None === href.fragment)
    assert(None === href.query)
    assert(None === href.file)
    assert(None === href.extension)
    assert(s === href.toString)
    assert(false === href.isURL)
    assert(!href.path.isAbsolute)
  }

  test("relative url with short path") {
    val s = "/"
    val href = Href(s)
    assert(None === href.endpoint)
    assert(true === href.path.isAbsolute)
    assert(Nil === href.path.segments)
    assert(None === href.fragment)
    assert(None === href.query)
    assert(None === href.file)
    assert(None === href.extension)
    assert(s === href.toString)
    assert(false === href.isURL)
    assert(href.path.isAbsolute)
  }

  test("relative url with path only") {
    val s = "/Addressing/URL/5_BNF.html"
    val href = Href(s)
    assert(None === href.endpoint)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert(None === href.fragment)
    assert(None === href.query)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(false === href.isURL)
    assert(href.path.isAbsolute)
  }

  test("relative url with fragment") {
    val s = "/Addressing/URL/5_BNF.html#z12"
    val href = Href(s)
    assert(None === href.endpoint)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert("z12" === href.fragment.get)
    assert(None === href.query)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(false === href.isURL)
  }

  test("relative url with port and query") {
    val s = "/Addressing/URL/5_BNF.html?red=yes"
    val href = Href(s)
    assert(None === href.endpoint)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert(None === href.fragment)
    assert("red=yes" === href.query.get)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(false === href.isURL)
  }

  test("relative url with port, fragment and query") {
    val s = "/Addressing/URL/5_BNF.html#there?red=yes"
    val href = Href(s)
    assert(None === href.endpoint)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert("there" === href.fragment.get)
    assert("red=yes" === href.query.get)
    assert("5_BNF.html" === href.file.get)
    assert("html" === href.extension.get)
    assert(s === href.toString)
    assert(false === href.isURL)
  }

  test("relative url with port, query and fragment") {
    val s = "/Addressing/URL/5_BNF.html?red=yes#there"
    val href = Href(s)
    assert(href.endpoint === None)
    assert(href.path.segments === List("Addressing", "URL", "5_BNF.html"))
    assert(href.fragment === Some("there"))
    assert(href.query === Some("red=yes"))
    assert(href.file === Some("5_BNF.html"))
    assert(href.extension === Some("html"))
    assert(href.toString === "/Addressing/URL/5_BNF.html#there?red=yes")
    assert(href.isURL === false)
  }

  test("withQuery augmentation") {
    val url = "http://www.w3.org/page"
    val href = Href(url)
    assert(url + "?a=hello+world&b=a%40b.com" === href.withQuery(Map("a" -> "hello world", "b" -> "a@b.com")).toString)
    assert(url === href.withQuery(Map()).toString)
  }

  test("file url should parse correctly with triple-slash") {
    val url = "file:///some/dir/name.ext"
    val href = Href(url)
    assert(href.endpoint.get.scheme === "file")
    assert(href.endpoint.get.hostAndPort === "")
    assert(href.pathString === "/some/dir/name.ext")
    assert(href.path.length === 3)
    assert(href.file === Some("name.ext"))
    assert(href.extension === Some("ext"))
    assert(href.asURL === new URL(url))
  }

  test("file url should parse correctly with single-slash") {
    val url = "file:/some/dir/name.ext"
    val href = Href(url)
    assert(href.endpoint.get.scheme === "file")
    assert(href.endpoint.get.hostAndPort === "")
    assert(href.pathString === "/some/dir/name.ext")
    assert(href.path.length === 3)
    assert(href.file === Some("name.ext"))
    assert(href.extension === Some("ext"))
    assert(href.asURL === new URL(url))
  }

  test("ftp url should parse correctly with username") {
    val url = "ftp://user@somewhere.com/some/dir/name.ext"
    val href = Href(url)
    assert(href.endpoint.get.scheme === "ftp")
    assert(href.endpoint.get.userinfo === Some("user"))
    assert(href.endpoint.get.hostAndPort === "somewhere.com")
    assert(href.pathString === "/some/dir/name.ext")
    assert(href.path.length === 3)
    assert(href.file === Some("name.ext"))
    assert(href.extension === Some("ext"))
    assert(href.asURL === new URL(url))
  }
}
