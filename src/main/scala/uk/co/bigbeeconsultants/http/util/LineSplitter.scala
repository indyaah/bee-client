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
 * This simple splitter provides a low-memory way of splitting large strings. No copying is needed;
 * instead the string is indexed to obtain successive sub-strings between separator characters. The
 * behaviour is essentially the same as String.split(Char), except that it is presented as an Iterator[String]
 * instead of as an array.
 */
class LineSplitter(string: String) extends Iterator[String] {
  private var s = 0
  private var e = findNextCrNl(0)

  def hasNext = s < string.length

  def next(): String = {
    assert(s <= e)
    val part = string.substring(s, e)
    s = skipNextCrNl(e)
    e = findNextCrNl(s)
    part
  }

  private def findNextCrNl(from: Int): Int = {
    var i = from
    while (i < string.length) {
      string(i) match {
        case '\n' => return i
        case '\r' => return i
        case _ => i += 1
      }
    }
    i
  }

  private def skipNextCrNl(from: Int): Int = {
    if (from < string.length) {
      string(from) match {
        case '\n' =>
          from + 1
        case '\r' =>
          string(from + 1) match {
            case '\n' =>
              from + 2
            case _ =>
              from + 1
          }
      }
    }
    else
      string.length
  }
}
