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

import uk.co.bigbeeconsultants.http._
import java.io.{InputStreamReader, BufferedReader, InputStream}
import java.nio.charset.Charset
import java.nio.{CharBuffer, ByteBuffer}

/**
 * Implements an InputStream filter that allows line-by-line processing and/or transcoding of the content.
 * @param source the upstream input filter
 * @param lineFilter a mutation function that will be applied to each line of text
 * @param sourceEncoding the upstream character encoding
 * @param downstreamEncoding the downstream character encoding
 */
class LineFilterInputStream(source: InputStream, lineFilter: TextFilter,
                            sourceEncoding: Charset, downstreamEncoding: Charset) extends InputStream {

  /**
   * Implements an InputStream filter that allows transcoding of the content.
   * @param source the upstream input filter
   * @param sourceEncoding the upstream character encoding
   * @param downstreamEncoding the downstream character encoding
   */
  def this(source: InputStream, sourceEncoding: Charset, downstreamEncoding: Charset) =
    this(source, NoChangeTextFilter, sourceEncoding, downstreamEncoding)

  /**
   * Implements an InputStream filter that allows line-by-line processing of the content.
   * @param source the upstream input filter
   * @param lineFilter a mutation function that will be applied to each line of text
   * @param encoding the character encoding
   */
  def this(source: InputStream, lineFilter: TextFilter, encoding: Charset) =
    this(source, lineFilter, encoding, encoding)

  /**
   * Implements an InputStream filter that allows line-by-line processing of the content, using UTF-8 encoding.
   * @param source the upstream input filter
   * @param lineFilter a mutation function that will be applied to each line of text
   */
  def this(source: InputStream, lineFilter: TextFilter) =
    this(source, lineFilter, Charset.forName("UTF-8"))

  private val reader = new BufferedReader(new InputStreamReader(source, sourceEncoding))
  private val encoder = downstreamEncoding.newEncoder()

  private var byteBuffer: ByteBuffer = _
  private var reading = true
  private var newline = ""

  fill()

  private def fill() {
    if (reading) {
      val line = reader.readLine()
      if (line == null) reading = false
      else {
        val processed = newline + lineFilter(line)
        newline = "\n"
        val charBuffer = CharBuffer.wrap(processed)
        byteBuffer = encoder.encode(charBuffer)
        encoder.reset()
      }
    }
  }

  override def read(): Int = {
    if (reading && !byteBuffer.hasRemaining)
      fill()
    if (reading) byteBuffer.get() else -1
  }

  override def close() {
    source.close()
  }
}
