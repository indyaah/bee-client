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

package uk.co.bigbeeconsultants.http.performance

import uk.co.bigbeeconsultants.http.util.Bytes
import java.io.{BufferedOutputStream, FileOutputStream}

object LargeFileGenerator {

  def trimOff(str: String, unwanted: Int): String = {
    str.substring(0, str.length - unwanted)
  }

  val K = 1000L

  def sizeOf(size: String): Long = {
    val (digits, unit) =
      if (size.endsWith("GiB")) {
        (trimOff(size, 3), Bytes.GiB)
      }
      else if (size.endsWith("MiB")) {
        (trimOff(size, 3), Bytes.MiB)
      }
      else if (size.endsWith("KiB")) {
        (trimOff(size, 3), Bytes.KiB)
      }
      else if (size.endsWith("GB")) {
        (trimOff(size, 2), K * K * K)
      }
      else if (size.endsWith("MB")) {
        (trimOff(size, 2), K * K)
      }
      else if (size.endsWith("KB")) {
        (trimOff(size, 2), K)
      }
      else if (size.endsWith("B")) {
        (trimOff(size, 1), 1L)
      }
      else (size, 1L)
    digits.toLong * unit
  }

  def generate(size: Long, target: String) {
    val fos = new FileOutputStream(target)
    try {
      val bos = new BufferedOutputStream(fos)
      var n = 0
      while (n < size) {
        var c = '1'.toInt
        while (n < size && c <= '9') {
          bos.write(c.toByte)
          c += 1
          n += 1
        }
        if (n < size) {
          bos.write(10.toByte)
          n += 1
        }
      }
      bos.flush()
    } finally {
      fos.close()
    }
  }

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: LargeFileGenerator <size> <target-file>")
    } else {
      generate(sizeOf(args(0)), args(1))
    }
  }
}
