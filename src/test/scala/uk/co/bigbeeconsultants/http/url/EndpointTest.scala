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

class EndpointTest extends FunSuite {

  val w3Org = "www.w3.org"
  val w3OrgUrl = "http://" + w3Org
  val myserver8080 = "http://myserver:8080"

  test("endpoint without port nor trailing slash") {
    val endpoint = Endpoint(w3OrgUrl)
    assert("http" === endpoint.scheme)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert(w3OrgUrl === endpoint.toString)
    assert("www.w3.org" === endpoint.hostAndPort)
  }

  test("endpoint without port but with trailing slash") {
    val endpoint = Endpoint(w3OrgUrl + "/")
    assert("http" === endpoint.scheme)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert(w3OrgUrl === endpoint.toString)
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

  test("endpoint from components") {
    val endpoint = Endpoint(Some("http"), Some(w3Org), None).get
    assert("http" === endpoint.scheme)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert(w3OrgUrl === endpoint.toString)
    assert("www.w3.org" === endpoint.hostAndPort)
  }

  test("endpoint from nothing") {
    val endpoint = Endpoint(None, None, None)
    assert(None === endpoint)
  }

  test("endpoint failure from incomplete parameters") {
    intercept[IllegalArgumentException] {
      Endpoint(Some("http"), None, None)
    }
  }
}
