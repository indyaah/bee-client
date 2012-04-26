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

import java.net.URL
import org.scalatest.FunSuite

class DomainTest extends FunSuite {

  val ftpUrl1 = new URL ("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL ("http://www.w3.org/standards/webdesign/htmlcss")
  val httpsUrl1 = new URL ("https://www.w3.org/login/")


  test ("domain isIpAddress with name") {
    val d = Domain ("alpha.bravo.charlie")
    expect (false)(d.isIpAddress)
  }


  test ("domain isIpAddress with dotted quad") {
    val d = Domain ("10.1.233.0")
    expect (true)(d.isIpAddress)
  }


  test ("domain matches same") {
    val d = Domain ("www.w3.org")
    expect (true)(d.matches (new URL ("http://www.w3.org:80/standards/")))
  }


  test ("domain matches parent") {
    val d = Domain ("w3.org")
    expect (true)(d.matches (httpUrl1))
  }


  test ("longer domain name is rejected") {
    val d = Domain ("members.w3.org")
    expect (false)(d.matches (httpUrl1))
  }


  test ("domain parent") {
    val d = Domain ("www.members.w3.org")
    expect ("www.members.w3.org")(d.domain)
    expect ("members.w3.org")(d.parent.get.domain)
    expect ("w3.org")(d.parent.get.parent.get.domain)
    expect (false)(d.parent.get.parent.get.parent.isDefined)
  }


  test ("localhost") {
    expect (true)(Domain.localhost.domain.length > 0)
  }
}
