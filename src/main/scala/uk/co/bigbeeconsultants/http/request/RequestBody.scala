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

package uk.co.bigbeeconsultants.http.request

import java.net.URLEncoder
import java.io.{OutputStreamWriter, PrintWriter, InputStream, OutputStream}
import uk.co.bigbeeconsultants.http._
import header.MediaType
import header.MediaType._
import util.HttpUtil._
import util.LineSplitter
import java.util

/**
 * Carries body data on a request. The body data is supplied by a closure using the
 * target HTTP output stream as its parameter, allowing data to be streamed from an arbitrary
 * source in order to minimise memory footprint, if required.
 * <p>
 * The companion object provides apply methods for common sources of body data.
 */
trait RequestBody {
  /** Gets the function that consumes this request body. */
  def copyTo(outputStream: OutputStream)

  /** Gets the content type. */
  def contentType: MediaType

  /** Gets a string representation of the body, if possible. Otherwise, "..." is returned. */
  def asString: String = "..."

  /**
   * Gets a byte array representation of the body, if possible. Some implementations do not provide this.
   *
   * Be careful with this array - you *should not* attempt to modify it, even though it is mutable
   * (which it is because otherwise an extra copy step would be needed, which would impair performance, and
   * the array would not be available to the many standard Java APIs that work with such data).
   */
  def asBytes: Array[Byte]

  /**
   * Gets a cached version of the body. The returned instance will provide an implementation of 'asBytes',
   * which is needed during digest authentication for example.
   */
  def cachedBody: RequestBody

  /** Gets the string representation and the content type for diagnostic purposes. */
  final def toShortString = {
    val as = asString
    val s = if (as.length > 125) as.substring(0, 125) + "..." else as
    "(" + s + "," + contentType + ")"
  }

  override def toString = "RequestBody" + toShortString

  /**
   * Equality between two instances is defined in terms of equality of both the byte content
   * and the media type.
   * @param other another instance, typically of a `RequestBody`
   * @return true iff two instances return the same results from `asBytes`, and they
   * share the same media type, then they are equal.
   */
  override def equals(other: Any) = {
    other match {
      case that: RequestBody =>
        (that canEqual this) &&
        util.Arrays.equals(this.asBytes, that.asBytes) &&
          this.contentType == that.contentType
      case _ => false
    }
  }

  def canEqual(other: Any) = other.isInstanceOf[RequestBody]

  /**
   * The hash code is computed from `asBytes` merged with the hash code from the media type.
   */
  override lazy val hashCode: Int = (41 * util.Arrays.hashCode(asBytes)) + contentType.hashCode

  protected val objectHashCode = super.hashCode
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Factory for request bodies.
 */
object RequestBody {

  /**
   * Factory for empty request bodies. An empty body differs from no body at all because it has a media type.
   */
  def apply(contentType: MediaType): RequestBody = new StringRequestBody("", contentType)

  /**
   * Factory for request bodies sourced from strings.
   */
  def apply(string: String, contentType: MediaType): RequestBody = new StringRequestBody(string, contentType)

  /**
   * Factory for request bodies sourced from key-value pairs, typical for POST requests.
   */
  def apply(data: Map[String, String], contentType: MediaType = APPLICATION_FORM_URLENCODED): RequestBody = {
    val b = new StringBuilder
    val encoding = contentType.charsetOrElse(HttpClient.UTF8)
    var amp = ""
    for ((key, value) <- data) {
      b.append(amp)
      b.append(URLEncoder.encode(key, encoding))
      b.append('=')
      b.append(URLEncoder.encode(value, encoding))
      amp = "&"
    }
    new StringRequestBody(b.toString(), contentType)
  }

  /**
   * Factory for request bodies sourced from binary data.
   */
  def apply(byteArray: Array[Byte], contentType: MediaType): RequestBody = new BinaryRequestBody(byteArray, contentType)

  /**
   * Factory for request bodies sourced from input streams. This copies the content from the input stream,
   * which it leaves unclosed.
   */
  def apply(inputStream: InputStream, contentType: MediaType): RequestBody =
    new StreamRequestBody((outputStream) => copyBytes(inputStream, outputStream), contentType)
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides a request body based simply on a string.
 * It is normally more convenient to construct instances via `RequestBody`.
 * @param string the source data
 * @param contentType the media type
 */
final class StringRequestBody(string: String, val contentType: MediaType,
                              rewrite: Option[TextFilter] = None) extends RequestBody {
  private def binaryCopyTo(outputStream: OutputStream) {
    outputStream.write(asBytes)
    outputStream.flush()
  }

  private def textCopyTo(outputStream: OutputStream) {
    val encoding = contentType.charsetOrElse(HttpClient.UTF8)
    val splitter = new LineSplitter(string)
    val pw = new PrintWriter(new OutputStreamWriter(outputStream, encoding))
    val filter = rewrite.get
    splitter.foreach {
      s => pw.println(filter(s))
    }
    pw.flush()
    outputStream.flush()
  }

  def copyTo(outputStream: OutputStream) {
    if (rewrite.isDefined) textCopyTo(outputStream)
    else binaryCopyTo(outputStream)
  }

  override def asString = string

  /** Gets a byte array representation of the body, if possible. Some implementations do not provide this. */
  lazy val asBytes: Array[Byte] = {
    val encoding = contentType.charsetOrElse(HttpClient.UTF8)
    string.getBytes(encoding)
  }

  /** Returns `this` because the implementation already contains cached data. */
  override def cachedBody = this
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides a request body based on an array of bytes that constitutes arbitrary data.
 * @param byteArray the data, whether binary or textual
 * @param contentType the media type
 */
final class BinaryRequestBody(byteArray: Array[Byte], val contentType: MediaType) extends RequestBody {
  def copyTo(outputStream: OutputStream) {
    outputStream.write(byteArray)
    outputStream.flush()
  }

  /**
   * If the content type is textual, this gets the byte array converted to a string. Otherwise it returns
   * a terse representation.
   */
  override def asString = {
    if (contentType.isTextual) {
      val encoding = contentType.charsetOrElse(HttpClient.UTF8)
      new String(byteArray, encoding)
    }
    else if (byteArray.length > 0) "..."
    else ""
  }

  /** Gets a byte array representation of the body, if possible. Some implementations do not provide this. */
  override def asBytes = byteArray

  /** Returns `this` because the implementation already contains cached data. */
  override def cachedBody = this
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides a request body based on an arbitrary function that writes data to an output stream, without any
 * inherent caching of the body data.
 * It is possible to obtain a cached version of this body; a `BinaryRequestBody` will be returned.
 * It is normally more convenient to construct instances via `RequestBody`.
 * @param copyToFn the writer function
 * @param contentType the media type
 */
final class StreamRequestBody(copyToFn: (OutputStream) => Unit, val contentType: MediaType) extends RequestBody {
  private var consumed = false

  def copyTo(outputStream: OutputStream) {
    if (consumed) throw new IllegalStateException("Cannot use the copyTo function more than once. Obtain a cachedBody and then use it instead.")
    consumed = true
    copyToFn(outputStream)
  }

  override def asBytes = throw new UnsupportedOperationException("This request body has not yet been cached.")

  /** Converts this body to a new `BinaryRequestBody`. */
  override def cachedBody: RequestBody = {
    new BinaryRequestBody(captureBytes(copyTo), contentType)
  }

  override def equals(other: Any) = other match {
    case that: StreamRequestBody => this eq that
    case _ => false
  }

  override lazy val hashCode: Int = objectHashCode
}
