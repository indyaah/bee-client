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

  test("endpoint without port nor trailing slash") {
    val endpoint = Endpoint("http://www.w3.org")
    assert("http" === endpoint.scheme)
    assert(None === endpoint.userinfo)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert("http://www.w3.org" === endpoint.toString)
    assert("www.w3.org" === endpoint.authority)
    assert("www.w3.org" === endpoint.hostAndPort)
  }

  test("endpoint without port but with trailing slash") {
    val endpoint = Endpoint("http://www.w3.org/")
    assert("http" === endpoint.scheme)
    assert(None === endpoint.userinfo)
    assert("www.w3.org" === endpoint.host)
    assert(None === endpoint.port)
    assert("http://www.w3.org" === endpoint.toString)
    assert("www.w3.org" === endpoint.authority)
    assert("www.w3.org" === endpoint.hostAndPort)
  }

  test("endpoint with port but no trailing slash") {
    val endpoint = Endpoint("http://myserver:8080")
    assert("http" === endpoint.scheme)
    assert(None === endpoint.userinfo)
    assert("myserver" === endpoint.host)
    assert(Some(8080) === endpoint.port)
    assert("http://myserver:8080" === endpoint.toString)
    assert("myserver:8080" === endpoint.hostAndPort)
  }

  test("endpoint with port and trailing slash") {
    val endpoint = Endpoint("http://myserver:8080/")
    assert("http" === endpoint.scheme)
    assert(None === endpoint.userinfo)
    assert("myserver" === endpoint.host)
    assert(Some(8080) === endpoint.port)
    assert("http://myserver:8080" === endpoint.toString)
    assert("myserver:8080" === endpoint.authority)
    assert("myserver:8080" === endpoint.hostAndPort)
  }

  test("endpoint with userinfo and port and trailing slash") {
    val endpoint = Endpoint("http://john@myserver:8080/")
    assert("http" === endpoint.scheme)
    assert(Some("john") === endpoint.userinfo)
    assert("myserver" === endpoint.host)
    assert(Some(8080) === endpoint.port)
    assert("http://john@myserver:8080" === endpoint.toString)
    assert("john@myserver:8080" === endpoint.authority)
    assert("myserver:8080" === endpoint.hostAndPort)
  }

  test("endpoint should reject password information (without port)") {
    intercept[IllegalArgumentException] {
      Endpoint("http://john:mypassword@myserver/")
    }
  }

  test("endpoint should reject password information (with port)") {
    intercept[IllegalArgumentException] {
      Endpoint("http://john:mypassword@myserver:80/")
    }
  }

  test("endpoint from components") {
    val endpoint = Endpoint(Some("http"), Some("www.w3.org"), Some(8080)).get
    assert("http" === endpoint.scheme)
    assert(None === endpoint.userinfo)
    assert("www.w3.org" === endpoint.host)
    assert(Some(8080) === endpoint.port)
    assert("http://www.w3.org" + ":8080" === endpoint.toString)
    assert("www.w3.org:8080" === endpoint.hostAndPort)
  }

  test("endpoint from nothing should be None") {
    val endpoint = Endpoint(None, None, None)
    assert(None === endpoint)
  }

  test("endpoint should reject incomplete parameters") {
    intercept[IllegalArgumentException] {
      Endpoint(Some("http"), None, None)
    }
  }
}
