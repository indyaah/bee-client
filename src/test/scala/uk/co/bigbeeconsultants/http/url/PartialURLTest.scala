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

  val w3Org = "http://www.w3.org"
  val myserver8080 = "http://myserver:8080"

  test("endpoint without port nor trailing slash") {
    val endpoint = Endpoint(w3Org)
    assert("http" === endpoint.scheme)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert(w3Org === endpoint.toString)
    assert("www.w3.org" === endpoint.hostAndPort)
  }

  test("endpoint without port but with trailing slash") {
    val endpoint = Endpoint(w3Org + "/")
    assert("http" === endpoint.scheme)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert(w3Org === endpoint.toString)
    assert("www.w3.org" === endpoint.hostAndPort)
  }

  test("endpoint with port but no trailing slash") {
    val endpoint = Endpoint(myserver8080)
    assert("http" === endpoint.scheme)
    assert("myserver" === endpoint.host)
    assert(Some(8080) === endpoint.port)
    assert(myserver8080 === endpoint.toString)
    assert("myserver:8080" === endpoint.hostAndPort)
  }

  test("endpoint with port and trailing slash") {
    val endpoint = Endpoint(myserver8080 + "/")
    assert("http" === endpoint.scheme)
    assert("myserver" === endpoint.host)
    assert(Some(8080) === endpoint.port)
    assert(myserver8080 === endpoint.toString)
    assert("myserver:8080" === endpoint.hostAndPort)
  }

  test("absolute url without any path") {
    val u = new URL(w3Org)
    val purl = PartialURL(w3Org)
    assert(w3Org === purl.endpoint.get.toString)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(w3Org + "/" === purl.toString)
    assert(new URL(w3Org + "/") === purl.asURL)
    assert(purl === PartialURL(u))
    assert(purl.path.isAbsolute)
  }

  test("absolute url with short path") {
    val s = "http://www.w3.org/"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert(w3Org === purl.endpoint.get.toString)
    assert(true === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
    assert(purl.path.isAbsolute)
  }

  test("absolute url with path only") {
    val s = "http://www.w3.org/Addressing/URL/5_BNF.html"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert(w3Org === purl.endpoint.get.toString)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(u === purl.asURL)
    assert(purl === PartialURL(u))
    assert(purl.startsWith(PartialURL(w3Org)))
  }

  test("absolute url with fragment") {
    val s = w3Org + "/Addressing/URL/5_BNF.html#z12"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert(w3Org === purl.endpoint.get.toString)
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
    val s = myserver8080 + "/Addressing/URL/5_BNF.html?red=yes"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert(myserver8080 === purl.endpoint.get.toString)
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
    val s = myserver8080 + "/Addressing/URL/5_BNF.html#there?red=yes"
    val u = new URL(s)
    val purl = PartialURL(s)
    assert(myserver8080 === purl.endpoint.get.toString)
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
    assert(None === purl.endpoint)
    assert(false === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(false === purl.isURL)
    assert(!purl.path.isAbsolute)
  }

  test("relative url with short path") {
    val s = "/"
    val purl = PartialURL(s)
    assert(None === purl.endpoint)
    assert(true === purl.path.isAbsolute)
    assert(Nil === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert(None === purl.file)
    assert(None === purl.extension)
    assert(s === purl.toString)
    assert(false === purl.isURL)
    assert(purl.path.isAbsolute)
  }

  test("relative url with path only") {
    val s = "/Addressing/URL/5_BNF.html"
    val purl = PartialURL(s)
    assert(None === purl.endpoint)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert(None === purl.fragment)
    assert(None === purl.query)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert(s === purl.toString)
    assert(false === purl.isURL)
    assert(purl.path.isAbsolute)
  }

  test("relative url with fragment") {
    val s = "/Addressing/URL/5_BNF.html#z12"
    val purl = PartialURL(s)
    assert(None === purl.endpoint)
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
    assert(None === purl.endpoint)
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
    assert(None === purl.endpoint)
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
    assert(None === purl.endpoint)
    assert(List("Addressing", "URL", "5_BNF.html") === purl.path.segments)
    assert("there" === purl.fragment.get)
    assert("red=yes" === purl.query.get)
    assert("5_BNF.html" === purl.file.get)
    assert("html" === purl.extension.get)
    assert("/Addressing/URL/5_BNF.html#there?red=yes" === purl.toString)
    assert(false === purl.isURL)
  }

}
