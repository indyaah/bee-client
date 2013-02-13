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

package uk.co.bigbeeconsultants.http.response

import org.scalatest.FunSuite
import java.io.{InputStream, BufferedReader, InputStreamReader, ByteArrayInputStream}
import uk.co.bigbeeconsultants.http._

class LineFilterInputStreamTest extends FunSuite {

  test("simple two-line string with no substitution") {
    val str = "This string is not very long\nand has two lines."
    val bais = new ByteArrayInputStream(str.getBytes)
    val fis = new LineFilterInputStream(bais, NoChangeTextFilter)
    val reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))
    assert(reader.readLine() === "This string is not very long")
    assert(reader.readLine() === "and has two lines.")
    assert(reader.readLine() === null)
  }

  test("simple two-line string with trailing newline but with no substitution") {
    val str = "This string is not very long\nand has two lines.\n"
    val bais = new ByteArrayInputStream(str.getBytes)
    val fis = new LineFilterInputStream(bais, NoChangeTextFilter)
    val reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))
    assert(reader.readLine() === "This string is not very long")
    assert(reader.readLine() === "and has two lines.")
    assert(reader.readLine() === null)
  }

  test("longer string with substitution") {
    val n = 1000
    val s0 = " - this is rather longer than in the earlier tests and includes weird characters £ê¬"
    val str = new StringBuilder
    for (i <- 1 to n) {
      str.append(i)
      str.append(s0)
      str.append('\n')
    }
    val bais = new ByteArrayInputStream(str.toString().getBytes)
    val fis = new LineFilterInputStream(bais, (s) => s.replace("a", "â"))
    val reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))
    val se = " - this is râther longer thân in the eârlier tests ând includes weird chârâcters £ê¬"
    for (i <- 1 to n) {
      val expected = i + se
      assert(reader.readLine() === expected)
    }
    assert(reader.readLine() === null)
  }

  test("close method is called through") {
    var closed = false
    val bais = new ByteArrayInputStream("".getBytes) {
      override def close() { closed = true }
    }
    val fis = new LineFilterInputStream(bais, NoChangeTextFilter)
    fis.close()
    assert(closed)
  }
}
