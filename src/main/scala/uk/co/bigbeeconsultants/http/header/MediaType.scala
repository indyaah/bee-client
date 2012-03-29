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

package uk.co.bigbeeconsultants.http.header

import uk.co.bigbeeconsultants.http.{HttpClient, Util}

case class MediaType(`type`: String, subtype: String, charset: Option[String] = None) {

  import MediaType._

  def value = `type` + '/' + subtype

  /**Gets this media type in the form used within QualifiedValue. */
  def toQualifiedPart = {
    val qual = if (charset.isEmpty) Nil else List(Qualifier("charset", charset.get))
    QualifiedPart(value, qual)
  }

  override lazy val toString = toQualifiedPart.toString

  /**
   * Gets the character set, or returns a default value.
   */
  def charsetOrElse(defaultCharset: String) = if (charset.isEmpty) defaultCharset else charset.get

  /**
   * Gets the character set, or returns a default value.
   */
  def charsetOrUTF8 = if (charset.isEmpty) HttpClient.UTF8 else charset.get

  /**
   * Checks if the primary type is a wildcard.
   * @return true if the primary type is a wildcard
   */
  def isWildcardType = `type` == WILDCARD;

  /**
   * Checks if the subtype is a wildcard.
   * @return true if the subtype is a wildcard
   */
  def isWildcardSubtype = subtype == WILDCARD

  /**
   * Check if this media type is compatible with another media type. E.g.
   * image*&#47;* is compatible with image/jpeg, image/png, etc. The function is commutative.
   * @return true if the types are compatible, false otherwise.
   * @param other the media type to compare with
   */
  def isCompatible(other: MediaType) = {
    if (other == null)
      false
    else if (`type` == WILDCARD || other.`type` == WILDCARD)
      true
    else if (`type`.equalsIgnoreCase(other.`type`) && (subtype == WILDCARD || other.subtype == WILDCARD))
      true
    else
      `type`.equalsIgnoreCase(other.`type`) && subtype.equalsIgnoreCase(other.subtype)
  }

  /**
   * Creates a new instance with a different charset.
   */
  def withCharset(newCharset: String) = {
    require(newCharset != null && newCharset.length() > 0)
    new MediaType(`type`, subtype, Some(newCharset))
  }

  def isTextual = {
    `type` match {
      case "text" => true
      case "application" if (subtype == "json" || subtype == "xml" || subtype.endsWith("+xml")) => true
      case _ => false
    }
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
  val IMAGE_PNG = MediaType("image", "png")
  val IMAGE_JPG = MediaType("image", "jpeg")

  def apply(str: String) = {
    val qp = QualifiedPart.parse(str)
    val qualifier = if (qp.qualifier.isEmpty) {
      None
    } else {
      val q = qp.qualifier(0)
      if (q.label == "charset") Some(q.value) else None
    }
    val t2 = Util.divide(qp.value, '/')
    new MediaType(orWildcard(t2._1), orWildcard(t2._2), qualifier)
  }

  private def orWildcard(s: String) = if (s.length > 0) s else WILDCARD

  implicit def convertToString(mt: MediaType) = mt.toString
}
