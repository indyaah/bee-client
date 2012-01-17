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

package uk.co.bigbeeconsultants.lhc

case class MediaType(`type`: String, subtype: String, charset: Option[String] = None) extends Valuable {

  def value = `type` + '/' + subtype

  /** Gets the charset as a list of zero or one {@link Qualifier}. */
  def qualifier = if (charset.isEmpty) Nil else List(Qualifier("charset", charset.get))

  override def toString = {
    val qual = if (charset.isEmpty) {
      ""
    } else {
      "; charset=" + charset.get
    }
    value + qual
  }

  /**
   * Gets the character set, or returns a default value.
   */
  def charsetOrElse(defaultCharset: String) = if (charset.isEmpty) defaultCharset else charset.get

  /**
   * Checks if the primary type is a wildcard.
   * @return true if the primary type is a wildcard
   */
  def isWildcardType = `type` == MediaType.WILDCARD;

  /**
   * Checks if the subtype is a wildcard.
   * @return true if the subtype is a wildcard
   */
  def isWildcardSubtype = subtype == MediaType.WILDCARD

  /**
   * Check if this media type is compatible with another media type. E.g.
   * image*&#47;* is compatible with image/jpeg, image/png, etc. The function is commutative.
   * @return true if the types are compatible, false otherwise.
   * @param other the media type to compare with
   */
  def isCompatible(other: MediaType) = {
    if (other == null)
      false
    else if (`type` == MediaType.WILDCARD || other.`type` == MediaType.WILDCARD)
      true
    else if (`type`.equalsIgnoreCase(other.`type`) && (subtype == MediaType.WILDCARD || other.subtype == MediaType.WILDCARD))
      true
    else
      `type`.equalsIgnoreCase(other.`type`) && subtype.equalsIgnoreCase(other.subtype)
  }

  /**
   * Creates a new instance with a different charset.
   */
  def withCharset(newCharset: String) = {
    require(newCharset != null && newCharset.length() > 0)
    MediaType(`type`, subtype, Some(newCharset))
  }
}

object MediaType {
  /**The value of a type or subtype wildcard: "*" */
  val WILDCARD = "*"

  val STAR_STAR = MediaType(WILDCARD, WILDCARD)
  val APPLICATION_JSON = MediaType("application", "json")
  val APPLICATION_XML = MediaType("application", "xml")
  val APPLICATION_SVG_XML = MediaType("application", "svg+xml")
  val APPLICATION_ATOM_XML = MediaType("application", "atom+xml")
  val APPLICATION_XHTML_XML = MediaType("application", "xhtml+xml")
  val APPLICATION_OCTET_STREAM = MediaType("application", "octet-stream")
  val APPLICATION_FORM_URLENCODED = MediaType("application", "x-www-form-urlencoded")
  val MULTIPART_FORM_DATA = MediaType("multipart", "form-data")
  val TEXT_PLAIN = MediaType("text", "plain")
  val TEXT_XML = MediaType("text", "xml")
  val TEXT_HTML = MediaType("text", "html")

  def apply(str: String) = {
    val t1 = Util.divide(str, ';')
    val qualifier = if (t1._2.length > 0) {
      val q = Qualifier(t1._2.trim)
      assume(q.label == "charset")
      Some(q.value)
    } else {
      None
    }
    val t2 = Util.divide(t1._1, '/')
    val `type` = if (t2._1.length > 0) t2._1 else WILDCARD
    val subtype = if (t2._2.length > 0) t2._2 else WILDCARD
    new MediaType(`type`, subtype, qualifier)
  }
}
