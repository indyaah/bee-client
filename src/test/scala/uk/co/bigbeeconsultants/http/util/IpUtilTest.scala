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

package uk.co.bigbeeconsultants.http.util

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IpUtilTest extends FunSuite {

  test("IP v4") {
    assert(IpUtil.isIp4AddressSyntax("1.1.1.1"))
    val dt = new DiagnosticTimer
    for (i <- 1 to 1000) {
      assert(IpUtil.isIp4AddressSyntax("1.1.1.1"))
      assert(!IpUtil.isIp4AddressSyntax("1.1.1.1.1"))
      assert(IpUtil.isIp4AddressSyntax("123.45.67.89"))
      assert(!IpUtil.isIp4AddressSyntax("123:456.789.123"))
      assert(!IpUtil.isIp4AddressSyntax("123.a56.789.123a"))
      assert(!IpUtil.isIp4AddressSyntax("a"))
      assert(!IpUtil.isIp4AddressSyntax(""))
    }
    println(dt)
  }

  test("IP v6") {
    val dt = new DiagnosticTimer
    for (i <- 1 to 1000) {
      assert(!IpUtil.isIp6AddressSyntax(""))
      assert(!IpUtil.isIp6AddressSyntax("z"))
      assert(IpUtil.isIp6AddressSyntax("::1"))
      assert(IpUtil.isIp6AddressSyntax("ff02::1:ff00:0"))
      assert(IpUtil.isIp6AddressSyntax("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
      assert(IpUtil.isIp6AddressSyntax("2001:db8:85a3:0:0:8a2e:370:7334"))
      assert(IpUtil.isIp6AddressSyntax("2001:db8:85a3::8a2e:370:7334"))
      assert(!IpUtil.isIp6AddressSyntax("123.56.789.123"))
      assert(!IpUtil.isIp6AddressSyntax("123:0:789:123x"))
    }
    println(dt)
  }

  test("either") {
    assert(!IpUtil.isIpAddressSyntax(""))
    assert(!IpUtil.isIpAddressSyntax("z"))
    assert(IpUtil.isIpAddressSyntax("1.1.1.1"))
    assert(IpUtil.isIpAddressSyntax("::1"))
  }
}
