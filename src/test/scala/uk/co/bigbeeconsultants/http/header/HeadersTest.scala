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

package uk.co.bigbeeconsultants.http.header

import uk.co.bigbeeconsultants.http.header.HeaderName._
import org.scalatest.FunSuite

class HeadersTest extends FunSuite {

  test ("names") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    expect (List (HOST.name, ACCEPT.name, ACCEPT.name))(n.names)
  }

  test ("find") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    expect (List (HOST -> "localhost"))(n.filter (HOST.name))
    expect (List (ACCEPT -> "foo", ACCEPT -> "bar"))(n.filter (ACCEPT.name))
  }

  test ("get") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    expect (HOST -> "localhost")(n(HOST.name))
    expect (Some(ACCEPT -> "foo"))(n.get (ACCEPT.name))
  }

  test ("remove and add") {
    val n1 = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    val n2 = n1.remove(HOST)
    expect (2)(n2.size)
    expect (false)(n2.contains(HOST))
    expect (ACCEPT -> "foo")(n2(ACCEPT.name))

    val n3 = n2.add(HOST -> "server:8080")
    expect (3)(n3.size)
    expect (HOST -> "server:8080")(n3(HOST.name))

    val n4 = n1.remove(ACCEPT)
    expect (1)(n4.size)
    expect (HOST -> "localhost")(n4(HOST.name))
    expect (false)(n4.contains(ACCEPT))
  }

}
