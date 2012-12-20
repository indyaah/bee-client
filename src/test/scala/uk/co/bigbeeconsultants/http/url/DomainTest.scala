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

class DomainTest extends FunSuite {

  val ftpUrl1 = new URL("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL("http://www.w3.org/standards/webdesign/htmlcss")
  val httpsUrl1 = new URL("https://www.w3.org/login/")


  test("domain isIpAddress with name") {
    val d = Domain("alpha.bravo.charlie")
    assert(!d.isIpAddress)
  }


  test("domain isIpAddress with IPv4") {
    val d = Domain("10.1.233.0")
    assert(d.isIpAddress)
  }


  test("domain isIpAddress with IPv6") {
    val d = Domain("2001:db8:85a3::8a2e:370:7334")
    assert(d.isIpAddress)
  }


  test("domain isIpAddress matches") {
    val d = Domain("10.1.233.0")
    assert(d.matches(new URL("http://10.1.233.0/foobar")))
    assert(!d.matches(new URL("http://192.168.1.1/foobar")))
  }


  test("domain matches same") {
    val d = Domain("www.w3.org")
    assert(d.matches(new URL("http://www.w3.org:80/standards/")))
  }


  test("domain matches parent") {
    val d = Domain("w3.org")
    assert(d.matches(httpUrl1))
  }


  test("domain matches parent with dot") {
    val d = Domain(".w3.org")
    assert(d.matches(httpUrl1))
  }


  test("longer domain name is rejected") {
    val d = Domain("members.w3.org")
    assert(!d.matches(httpUrl1))
  }


  test("domain parent") {
    val d = Domain("www.members.w3.org")
    assert("www.members.w3.org" === d.domain)
    assert("members.w3.org" === d.parent.get.domain)
    assert("w3.org" === d.parent.get.parent.get.domain)
    assert(!d.parent.get.parent.get.parent.isDefined)
  }


  test("localhost") {
    assert(Domain.localhost.domain.length > 0)
  }
}
