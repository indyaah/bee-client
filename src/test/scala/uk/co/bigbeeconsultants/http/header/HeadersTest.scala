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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HeadersTest extends FunSuite {

  test ("construction using list") {
    val n = Headers (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar")
    assert (3 === n.size)
    assert (HOST -> "localhost" === n(0))
    assert (ACCEPT -> "foo" === n(1))
    assert (ACCEPT -> "bar" === n(2))
  }

  test ("construction using map") {
    val n = Headers (Map("Host" -> "localhost", "Accept" -> "foo"))
    assert (2 === n.size)
    assert (HOST-> "localhost" === n(0))
    assert (ACCEPT -> "foo" === n(1))
  }

  test ("names") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    assert (List (HOST.name, ACCEPT.name, ACCEPT.name) === n.names)
  }

  test ("contains") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    assert (n contains HOST)
    assert (n contains HeaderName("HOST"))
    assert (n contains ACCEPT)
    assert (n contains HeaderName("accept"))
  }

  test ("filter") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    assert (Headers (List (HOST -> "localhost")) === (n filter HOST))
    assert (Headers (List (HOST -> "localhost")) === (n filter HeaderName("HOST")))
    assert (Headers (List (ACCEPT -> "foo", ACCEPT -> "bar")) === (n filter ACCEPT))
    assert (Headers (List (ACCEPT -> "foo", ACCEPT -> "bar")) === (n filter HeaderName("accept")))
  }

  test ("get and apply") {
    val n = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    assert (HOST -> "localhost" === n(0))
    assert (HOST -> "localhost" === n(HOST))
    assert (Some(ACCEPT -> "foo") === (n get ACCEPT))
  }

  test ("remove and add") {
    val n1 = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    val n2 = n1 filterNot HOST
    assert (2 === n2.size)
    assert (false === (n2 contains HOST))
    assert (ACCEPT -> "foo" === n2(ACCEPT))

    val n3 = n2 + (HOST -> "server")
    assert (3 === n3.size)
    assert (HOST -> "server" === n3(HOST))

    val n4 = n1 filterNot ACCEPT
    assert (1 === n4.size)
    assert (HOST -> "localhost" === n4(HOST))
    assert (false === (n4 contains ACCEPT))
  }

  test ("set") {
    val n1 = Headers (List (HOST -> "localhost", ACCEPT -> "foo", ACCEPT -> "bar"))
    val n2 = n1 set (HOST -> "server")
    assert (3 === n2.size)
    assert (HOST -> "server" === n2(HOST))

    val n3 = n2 set (ACCEPT -> "*/*")
    assert (2 === n3.size)
    assert (HOST -> "server" === n3(HOST))
    assert (ACCEPT -> "*/*" === n3(ACCEPT))
  }

}
