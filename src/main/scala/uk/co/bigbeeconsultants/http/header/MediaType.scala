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

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.util.HttpUtil._
import uk.co.bigbeeconsultants.http.response.MimeTypeRegistry

/**
 * Provides a media type. Also known as a MIME type or content type.
 */
case class MediaType(mainType: String, subtype: String, charset: Option[String] = None) extends Value {

  import MediaType._

  def isValid = !mainType.isEmpty && !subtype.isEmpty

  /** Gets the main/sub type of this media type. That is, the textual representation without any charset. */
  val mediaType = mainType + '/' + subtype

  /** Gets `mediaType` and appends the charset, if there is one. */
  val value =
    if (charset.isEmpty) mediaType
    else mediaType + ";charset=" + charset.get

  /** Gets this media type as a Qualifiers, which is the form used within QualifiedValue. */
  def toQualifiers = Qualifiers(toString)

  /**
   * Gets the character set, or returns a default value.
   */
  def charsetOrElse(defaultCharset: String) = if (charset.isEmpty) defaultCharset else charset.get

  /**
   * Gets the character set, or returns UTF8.
   */
  def charsetOrUTF8 = charsetOrElse(HttpClient.UTF8)

  /**
   * Checks if the primary type is a wildcard.
   * @return true if the primary type is a wildcard
   */
  def isWildcardType = mainType == WILDCARD

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
    else if (mainType == WILDCARD || other.mainType == WILDCARD)
      true
    else if ((mainType equalsIgnoreCase other.mainType) && (subtype == WILDCARD || other.subtype == WILDCARD))
      true
    else
      (mainType equalsIgnoreCase other.mainType) && (subtype equalsIgnoreCase other.subtype)
  }

  /**
   * Creates a new instance with a different charset.
   */
  def withCharset(newCharset: String) = {
    require(newCharset != null && newCharset.length() > 0)
    new MediaType(mainType, subtype, Some(newCharset))
  }

  /**
   * Tests whether a media type represents textual traffic. This is true for all content with the `type` of
   * "text" and also for those "application" types with json, xml, or ...+xml subtypes.
   */
  val isTextual = {
    def isStandardXmlType = subtype endsWith "+xml"

    def isUnusualTextualType = MimeTypeRegistry.textualTypes.contains(value)

    mainType match {
      case "text" => true
      case _ => isStandardXmlType || isUnusualTextualType
    }
  }
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides some commonly-used `MediaType` instances and a factory constructor for new instances.
 */
object MediaType extends ValueParser[MediaType] {

  /** The value of a type or subtype wildcard: "*" */
  val WILDCARD = "*"

  val STAR_STAR = MediaType(WILDCARD, WILDCARD)

  val APPLICATION_JSON = MediaType("application", "json")
  val APPLICATION_JAVASCRIPT = MediaType("application", "javascript")
  val APPLICATION_ECMASCRIPT = MediaType("application", "ecmascript")
  val APPLICATION_PDF = MediaType("application", "pdf")
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
  val TEXT_CSS = MediaType("text", "css")
  val TEXT_CSV = MediaType("text", "csv")

  val IMAGE_PNG = MediaType("image", "png")
  val IMAGE_JPG = MediaType("image", "jpeg")
  val IMAGE_GIF = MediaType("image", "gif")
  val IMAGE_SVG_XML = MediaType("image", "svg+xml")

  /**
   * Constructs a new MediaType instance from a string typically as found in HTTP content-type values.
   */
  def apply(str: String): MediaType = {
    val qp = Qualifiers(str)
    val t2 = divide(qp(0).name, '/')
    if (qp.qualifiers.size == 1) {
      new MediaType(orWildcard(t2._1), orWildcard(t2._2), None)
    } else {
      val q = qp.qualifiers(1)
      val charset = if (q.name == "charset") q.value else None
      new MediaType(orWildcard(t2._1), orWildcard(t2._2), charset)
    }
  }

  def ifValid(value: String) = {
    val v = apply(value)
    if (v.isValid) Some(v) else None
  }

  private def orWildcard(s: String) = if (s.length > 0) s else WILDCARD

  implicit def convertToString(mt: MediaType) = mt.value
}
