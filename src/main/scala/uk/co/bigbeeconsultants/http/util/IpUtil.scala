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

/**
 * Provides utilities for internet addresses.
 */
object IpUtil {
//  val ipv4pattern = Pattern.compile("[12]?[0-9]?[0-9]\\.[12]?[0-9]?[0-9]\\.[12]?[0-9]?[0-9]\\.[12]?[0-9]?[0-9]")

  /**
   * Tests the syntax of a string to see whether it may be an IPv4 or IPv6 address. This does not confirm whether it
   * is a valid address, just that the syntax is valid.
   */
  def isIpAddressSyntax(host: String) = isIp4AddressSyntax(host) || isIp6AddressSyntax(host)

  /**
   * Tests the syntax of a string to see whether it may be an IPv4 address. This does not confirm whether it
   * is a valid address, just that the syntax is valid.
   */
  def isIp4AddressSyntax(host: String) = {
    // regex is at least two or three times slower
    // ipv4pattern.matcher(host).matches()

    val chars = host.toCharArray
    var result = true
    var dots = 0
    var groupSize = 0
    var i = 0
    while (result && i < chars.length) {
      val c = chars(i)
      if (c == '.') {
        dots += 1
        result = 0 < groupSize && groupSize <= 3
        groupSize = 0
      }
      else if (Character.isDigit(c)) groupSize += 1
      else result = false
      i += 1
    }
    result && dots == 3 && 0 < groupSize && groupSize <= 3
  }


  /**
   * Tests the syntax of a string to see whether it may be an IPv6 address. This does not confirm whether it
   * is a valid address, just that the syntax is valid.
   */
  def isIp6AddressSyntax(host: String) = {
    val chars = host.toCharArray
    var result = chars.length >= 3
    var i = 0
    while (result && i < chars.length) {
      val c = chars(i)
      if (c != ':' && !Character.isDigit(c) && !isHex(c)) result = false
      i += 1
    }
    result
  }

  private def isHex(c: Char) = {
    ('A' <= c && c <= 'F') || ('a' <= c && c <= 'f')
  }
}
