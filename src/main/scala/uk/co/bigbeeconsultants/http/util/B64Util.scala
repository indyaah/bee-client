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

object B64Util {

  private val base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
    "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "+/"

  private val splitLinesAt = 76

  private def zeroPad(length: Int, bytes: Array[Byte]): Array[Byte] = {
    val padded = new Array[Byte](length) // initialized to zero by JVM
    System.arraycopy(bytes, 0, padded, 0, bytes.length)
    padded
  }

  def encode76(string: String): String = splitLinesAt76(encode(string))

  def encode(string: String): String = { //Base64.encodeBytes(string.getBytes("UTF-8"))
    val encoded = new StringBuilder
    val stringArray = string.getBytes("UTF-8")

    // determine how many padding bytes to add to the output
    val paddingCount = (3 - (stringArray.length % 3)) % 3

    // add any necessary padding to the input
    val paddedArray = zeroPad(stringArray.length + paddingCount, stringArray)

    // process 3 bytes at a time, churning out 4 output bytes
    // worry about CRLF insertions later
    var i = 0
    while (i < paddedArray.length) {
      val j = ((paddedArray(i) & 0xff) << 16) +
        ((paddedArray(i + 1) & 0xff) << 8) +
        (paddedArray(i + 2) & 0xff)
      encoded.append(base64code.charAt((j >> 18) & 0x3f))
      encoded.append(base64code.charAt((j >> 12) & 0x3f))
      encoded.append(base64code.charAt((j >> 6) & 0x3f))
      encoded.append(base64code.charAt(j & 0x3f))
      i = i + 3
    }

    // replace encoded padding nulls with "="
    val enc = encoded.toString
    val padding = "==".substring(0, paddingCount)
    enc.substring(0, enc.length - paddingCount) + padding
  }

  def splitLinesAt76(string: String): String = {
    val lines = new StringBuilder
    var i = 0
    var crnl = ""
    while (i < string.length) {
      lines.append(crnl)
      lines.append(string.substring(i, Math.min(string.length(), i + splitLinesAt)))
      crnl = newline
      i += splitLinesAt
    }
    lines.toString
  }

  //def decode(b64: String): String = b64 // TODO

  val newline = "\r\n"
}
