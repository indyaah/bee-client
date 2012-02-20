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

package uk.co.bigbeeconsultants.lhc.header

import org.junit.Test
import org.junit.Assert._
import java.net.URL

class DomainTest {

  val ftpUrl1 = new URL("ftp://www.w3.org/standards/webdesign/htmlcss")
  val httpUrl1 = new URL("http://www.w3.org/standards/webdesign/htmlcss")
  val httpsUrl1 = new URL("https://www.w3.org/login/")

  @Test
  def domain_isIpAddress_withName() {
    val d = Domain("alpha.bravo.charlie")
    assertFalse(d.isIpAddress)
  }

  @Test
  def domain_isIpAddress_withDottedQuad() {
    val d = Domain("10.1.233.0")
    assertTrue(d.isIpAddress)
  }

  @Test
  def domain_matchesSame() {
    val d = Domain("www.w3.org")
    assertTrue(d.matches(new URL("http://www.w3.org:80/standards/")))
  }

  @Test
  def domain_matchesParent() {
    val d = Domain("w3.org")
    assertTrue(d.matches(httpUrl1))
  }

  @Test
  def longerDomainName_isRejected() {
    val d = Domain("members.w3.org")
    assertFalse(d.matches(httpUrl1))
  }
}
