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

package uk.co.bigbeeconsultants.http.request

import org.scalatest.FunSuite
import java.net.URL

class SplitURLTest extends FunSuite {

  test("very simple url") {
    val s = "http://www.w3.org/"
    val url = new URL(s)
    val surl = SplitURL(url)
    assert("http" === surl.scheme)
    assert("www.w3.org" === surl.host)
    assert(None === surl.port)
    assert(Nil === surl.pathSegments)
    assert(None === surl.fragment)
    assert(None === surl.query)
    assert(None === surl.file)
    assert(None === surl.extension)
    assert(s === surl.toString)
  }

  test("quite simple url") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html"
    val url = new URL(s)
    val surl = SplitURL(url)
    assert("http" === surl.scheme)
    assert("www.w3.org" === surl.host)
    assert(None === surl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === surl.pathSegments)
    assert(None === surl.fragment)
    assert(None === surl.query)
    assert(Some("5_BNF.html") === surl.file)
    assert(Some("html") === surl.extension)
    assert(s === surl.toString)
  }

  test("absolute url with fragment") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html#z12"
    val url = new URL(s)
    val surl = SplitURL(url)
    assert("http" === surl.scheme)
    assert("www.w3.org" === surl.host)
    assert(None === surl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === surl.pathSegments)
    assert(Some("z12") === surl.fragment)
    assert(None === surl.query)
    assert(Some("5_BNF.html") === surl.file)
    assert(Some("html") === surl.extension)
    assert(s === surl.toString)
  }

  test("absolute url with port and query") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html?red=yes"
    val url = new URL(s)
    val surl = SplitURL(url)
    assert("http" === surl.scheme)
    assert("myserver" === surl.host)
    assert(Some(8080) === surl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === surl.pathSegments)
    assert(None === surl.fragment)
    assert(Some("red=yes") === surl.query)
    assert(Some("5_BNF.html") === surl.file)
    assert(Some("html") === surl.extension)
    assert(s === surl.toString)
  }

  test("string parser 1a") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html#z12?red=yes"
    val surl = SplitURL(s)
    assert("http" === surl.scheme)
    assert("myserver" === surl.host)
    assert(Some(8080) === surl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === surl.pathSegments)
    assert(Some("z12") === surl.fragment)
    assert(Some("red=yes") === surl.query)
    assert(Some("5_BNF.html") === surl.file)
    assert(Some("html") === surl.extension)
    assert(s === surl.toString)
  }

  test("string parser 1b") {
    val s = "http://myserver/"
    val surl = SplitURL(s)
    assert("http" === surl.scheme)
    assert("myserver" === surl.host)
    assert(None === surl.port)
    assert(Nil === surl.pathSegments)
    assert(None === surl.fragment)
    assert(None === surl.query)
    assert(s === surl.toString)
  }

  test("string parser 2a") {
    val s = "http://myserver/"
    val surl = SplitURL("http", "myserver", -1, "", null, null)
    assert("http" === surl.scheme)
    assert("myserver" === surl.host)
    assert(None === surl.port)
    assert(Nil === surl.pathSegments)
    assert(None === surl.fragment)
    assert(None === surl.query)
    assert(s === surl.toString)
  }

  test("string parser 2b") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html?red=yes"
    val surl = SplitURL("http", "myserver", 8080, "/Addressing/URL/5_BNF.html", null, "red=yes")
    assert("http" === surl.scheme)
    assert("myserver" === surl.host)
    assert(Some(8080) === surl.port)
    assert(List("Addressing", "URL", "5_BNF.html") === surl.pathSegments)
    assert(None === surl.fragment)
    assert(Some("red=yes") === surl.query)
    assert(s === surl.toString)
  }

  test("convertToURL implicit converter") {
    val url = new URL("http://www.w3.org/")
    val surl = SplitURL(url)
    val url2: URL = surl
    assert(url === url2)
  }

  test("convertFromURL implicit converter") {
    val url = new URL("http://www.w3.org/")
    val surl: SplitURL = url
    assert(url === surl.asURL)
  }

  test("convertFromString implicit converter") {
    val url = "http://www.w3.org/"
    val surl: SplitURL = url
    assert(url === surl.toString)
  }

  test("withQuery augmentation") {
    val url = "http://www.w3.org/page"
    val surl = SplitURL(url)
    assert(url + "?a=hello+world&b=a%40b.com" === surl.withQuery(Map("a" -> "hello world", "b" -> "a@b.com")).toString)
    assert(url === surl.withQuery(Map()).toString)
  }
}
