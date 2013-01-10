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

import collection.mutable.ListBuffer
import java.nio.ByteBuffer
import java.io._
import uk.co.bigbeeconsultants.http._

object HttpUtil {
  // Known to be immutable.
  val emptyBuffer = ByteBuffer.allocateDirect(0)

  val DEFAULT_BUFFER_SIZE = 1024 * 16

  /**
   * Removes surrounding double quotes from a string.
   */
  def unquote(str: String): String = {
    if (str.length >= 2 && str(0) == '"' && str(str.length - 1) == '"') str.substring(1, str.length - 1)
    else str
  }

  /**
   * Efficiently splits a string at all occurrences of a given character. The returned list always contains at least
   * one item, even if it is blank.
   */
  def split(str: String, sep: Char): List[String] = {
    val list = new ListBuffer[String]
    val part = new StringBuilder
    for (c <- str.toCharArray) {
      if (c == sep) {
        list += part.toString()
        part.setLength(0)
      } else {
        part += c
      }
    }
    list += part.toString()
    list.toList
  }

  /**
   * Efficiently splits a string at all occurrences of a given character except those inside double quotes.
   * The returned list always contains at least one item, even if it is blank.
   */
  def splitQuoted(str: String, sep: Char, quoteMark: Char = '"'): List[String] = {
    val list = new ListBuffer[String]
    val part = new StringBuilder
    var inQuotes = false
    for (c <- str.toCharArray) {
      if (c == sep) {
        if (inQuotes) {
          part += c
        } else {
          list += part.toString()
          part.setLength(0)
        }
      } else if (c == quoteMark) {
        inQuotes = !inQuotes
        part += c
      } else {
        part += c
      }
    }
    list += part.toString()
    list.toList
  }

  /**
   * Efficiently divides a string at the first occurrence of a separator character.
   * @return a tuple of the string before and the string after the separator
   */
  def divide(str: String, sep: Char) = {
    val s = str.indexOf(sep)
    if (s >= 0 && s < str.length) {
      val a = str.substring(0, s)
      val b = str.substring(s + 1)
      (a, b)
    }
    else (str, "")
  }

  /**
   * Copies the bytes from an input stream into a new byte buffer, then closes the input stream.
   * @param inputStream the input stream
   * @return the new byte buffer
   */
  @throws(classOf[IOException])
  def copyToByteBufferAndClose(inputStream: InputStream): ByteBuffer = {
    val initialSize = 0x10000 // 64K
    copyToByteBufferAndClose(inputStream, initialSize)
  }

  /**
   * Copies the bytes from an input stream into a new byte buffer, then closes the input stream.
   * @param inputStream the input stream
   * @param initialSize the initial size of the buffer
   * @return the new byte buffer
   */
  @throws(classOf[IOException])
  def copyToByteBufferAndClose(inputStream: InputStream, initialSize: Int): ByteBuffer = {
    if (initialSize > 0) {
      val outStream = new ByteArrayOutputStream(initialSize)
      if (inputStream != null) {
        copyBytes(inputStream, outStream)
        inputStream.close()
      }
      ByteBuffer.wrap(outStream.toByteArray)
    } else {
      emptyBuffer
    }
  }

  /**
   * Directly copies the bytes from an input stream to an output stream, using only a small intermediate buffer.
   * @param input the input stream
   * @param output the output stream
   * @return the number of bytes copied
   */
  @throws(classOf[IOException])
  def copyBytes(input: InputStream, output: OutputStream): Long = {
    val buffer: Array[Byte] = new Array[Byte](DEFAULT_BUFFER_SIZE)
    var count: Long = 0
    if (input != null) {
      var n = input.read(buffer)
      while (n >= 0) {
        output.write(buffer, 0, n)
        count += n
        n = input.read(buffer)
      }
      output.flush()
    }
    count
  }

  /**
   * Directly copies the bytes from a buffer to an output stream.
   * @param bytes the input stream
   * @param output the output stream
   * @return the number of bytes copied
   */
  @throws(classOf[IOException])
  def copyArray(bytes: Array[Byte], output: OutputStream): Long = {
    output.write(bytes)
    output.flush()
    bytes.length
  }

  /**
   * Copies the text from an input stream to an output stream line by line, allowing alteration of the text.
   * This might be used to rewrite URLs in body content, for example.
   * @param input the input stream
   * @param output the output stream
   * @param charset the character set, default UTF-8
   * @param alter an optional function for changing each line of text before writing it out. This is applied
   *              line by line.
   */
  @throws(classOf[IOException])
  def copyText(input: InputStream, output: OutputStream, charset: String = HttpClient.UTF8,
               alter: TextFilter = NoChangeTextFilter) {
    if (input != null && output != null) {
      val in = new BufferedReader(new InputStreamReader(input, charset))
      val out = new PrintWriter(new OutputStreamWriter(output, charset))
      var line = in.readLine
      while (line != null) {
        out.println(alter(line))
        line = in.readLine
      }
      out.flush()
    }
  }

  /**
   * Copies the text from a string to an output stream line by line, allowing alteration of the text.
   * This might be used to rewrite URLs in body content, for example.
   * @param string the source text
   * @param output the output stream
   * @param charset the character set, default UTF-8
   * @param alter an optional function for changing each line of text before writing it out. This is applied
   *              line by line.
   */
  @throws(classOf[IOException])
  def copyString(string: String, output: OutputStream, charset: String = HttpClient.UTF8,
                 alter: TextFilter = NoChangeTextFilter) {
    if (output != null) {
      val out = new PrintWriter(new OutputStreamWriter(output, charset))
      val last = string.length - 1
      val s = if (last >= 0 && string.charAt(last) == '\n') string.substring(0, last) else string
      for (line <- new Splitter(s, '\n')) {
        out.println(alter(line))
      }
      out.flush()
    }
  }

  /**
   * Captures the bytes from an output stream in a buffer.
   * @param copyTo the function that recevies an output stream
   * @return the byte array
   */
  def captureBytes(copyTo: (OutputStream) => Unit) = {
    val baos = new ByteArrayOutputStream
    copyTo(baos)
    baos.toByteArray
  }
}
