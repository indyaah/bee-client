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
    expect("http")(surl.scheme)
    expect("www.w3.org")(surl.host)
    expect(None)(surl.port)
    expect(Nil)(surl.pathSegments)
    expect(None)(surl.fragment)
    expect(None)(surl.query)
    expect(s)(surl.toString)
  }

  test("quite simple url") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html"
    val url = new URL(s)
    val surl = SplitURL(url)
    expect("http")(surl.scheme)
    expect("www.w3.org")(surl.host)
    expect(None)(surl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(surl.pathSegments)
    expect(None)(surl.fragment)
    expect(None)(surl.query)
    expect(s)(surl.toString)
  }

  test("absolute url with fragment") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html#z12"
    val url = new URL(s)
    val surl = SplitURL(url)
    expect("http")(surl.scheme)
    expect("www.w3.org")(surl.host)
    expect(None)(surl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(surl.pathSegments)
    expect(Some("z12"))(surl.fragment)
    expect(None)(surl.query)
    expect(s)(surl.toString)
  }

  test("absolute url with port and query") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html?red=yes"
    val url = new URL(s)
    val surl = SplitURL(url)
    expect("http")(surl.scheme)
    expect("myserver")(surl.host)
    expect(Some(8080))(surl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(surl.pathSegments)
    expect(None)(surl.fragment)
    expect(Some("red=yes"))(surl.query)
    expect(s)(surl.toString)
  }

  test("string parser 1a") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html#z12?red=yes"
    val surl = SplitURL(s)
    expect("http")(surl.scheme)
    expect("myserver")(surl.host)
    expect(Some(8080))(surl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(surl.pathSegments)
    expect(Some("z12"))(surl.fragment)
    expect(Some("red=yes"))(surl.query)
    expect(s)(surl.toString)
  }

  test("string parser 1b") {
    val s = "http://myserver/"
    val surl = SplitURL(s)
    expect("http")(surl.scheme)
    expect("myserver")(surl.host)
    expect(None)(surl.port)
    expect(Nil)(surl.pathSegments)
    expect(None)(surl.fragment)
    expect(None)(surl.query)
    expect(s)(surl.toString)
  }

  test("string parser 2a") {
    val s = "http://myserver/"
    val surl = SplitURL("http", "myserver", -1, "", null, null)
    expect("http")(surl.scheme)
    expect("myserver")(surl.host)
    expect(None)(surl.port)
    expect(Nil)(surl.pathSegments)
    expect(None)(surl.fragment)
    expect(None)(surl.query)
    expect(s)(surl.toString)
  }

  test("string parser 2b") {
    val s = "http://myserver:8080/Addressing/URL/5_BNF.html?red=yes"
    val surl = SplitURL("http", "myserver", 8080, "/Addressing/URL/5_BNF.html", null, "red=yes")
    expect("http")(surl.scheme)
    expect("myserver")(surl.host)
    expect(Some(8080))(surl.port)
    expect(List("Addressing", "URL", "5_BNF.html"))(surl.pathSegments)
    expect(None)(surl.fragment)
    expect(Some("red=yes"))(surl.query)
    expect(s)(surl.toString)
  }

  test("convertToURL implicit converter") {
    val url = new URL("http://www.w3.org/")
    val surl = SplitURL(url)
    val url2: URL = surl
    expect(url)(url2)
  }

  test("convertFromURL implicit converter") {
    val url = new URL("http://www.w3.org/")
    val surl: SplitURL = url
    expect(url)(surl.asURL)
  }

  test("convertFromString implicit converter") {
    val url = "http://www.w3.org/"
    val surl: SplitURL = url
    expect(url)(surl.toString)
  }
}
