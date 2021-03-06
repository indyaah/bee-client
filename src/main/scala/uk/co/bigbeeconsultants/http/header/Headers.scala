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

import collection.immutable.List

/**
 * Holds a list of headers. Remember that header names are case-insensitive; the get, find, etc methods take
 * this into account.
 */
case class Headers(list: List[Header]) {

  def iterator = list.iterator

  def foreach[U](f: Header => U) {
    iterator.foreach(f)
  }

  def isEmpty = list.isEmpty

  def size = list.size

  /** Gets the list of header names. */
  def names: List[String] = list.map(_.name)

  /**
   * Tests whether a given header is present.
   * @param name the required header name. Uppercase or lowercase doesn't matter. Via an implicit
   *             conversion, a String can be used here.
   */
  def contains(name: HeaderName) = list.exists(_ =~= name)

  /**
   * Finds all the headers that have a given name.
   * @param name the required header name. Uppercase or lowercase doesn't matter. Via an implicit
   *             conversion, a String can be used here.
   */
  def filter(name: HeaderName): Headers = new Headers(list.filter(_ =~= name))

  /**
   * Finds all the headers that do not have a given name.
   * @param name the required header name. Uppercase or lowercase doesn't matter. Via an implicit
   *             conversion, a String can be used here.
   */
  def filterNot(name: HeaderName): Headers = new Headers(list.filterNot(_ =~= name))

  /**
   * Finds the header that has a given name. If more than one match exists, only the first
   * will be returned. Use 'filter' if you anticipate more than one.
   * @param name the required header name. Uppercase or lowercase doesn't matter. Via an implicit
   *             conversion, a String can be used here.
   */
  def get(name: HeaderName): Option[Header] = list.find(_ =~= name)

  /**
   * Finds the header that has a given name. If none exists, an exception will be thrown.
   * If more than one match exists, only the first will be returned.
   * @param name the required header name. Uppercase or lowercase doesn't matter. Via an implicit
   *             conversion, a String can be used here.
   */
  def apply(name: HeaderName): Header = list.find(_ =~= name).get

  /**
   * Gets the header at a specified index.
   */
  def apply(index: Int): Header = list(index)

  /**
   * Creates a new Headers with an extra header prepended. If this header is already present,
   * this method has the effect of adding another header with the same name.
   */
  def add(newHeader: Header): Headers = {
    new Headers(newHeader :: list)
  }

  /**
   * Creates a new Headers augmented with a specified header. If this header is already present, it is removed,
   * so this method has the effect of replacing the existing value(s). Otherwise it adds a new header.
   */
  def set(newHeader: Header): Headers = {
    new Headers(newHeader :: list.filterNot(_ =~= newHeader.name))
  }

  /**
   * An alias for add.
   */
  def +(newHeader: Header): Headers = {
    add(newHeader)
  }

  /**
   * Conjoins two instances to produce a new instance that includes all the headers from both.
   */
  def ++(newHeaders: Headers): Headers = {
    new Headers(this.list ++ newHeaders.list)
  }

  import HeaderName._
  lazy val ageHdr: Option[Long] = get(AGE).flatMap(_.toNumber)
  lazy val wwwAuthenticateHdrs: List[AuthenticateValue] = list.filter(_ =~= WWW_AUTHENTICATE).flatMap(_.toAuthenticateValue)
  lazy val cacheControlHdr: Option[CacheControlValue] = get(CACHE_CONTROL).flatMap(_.toCacheControlValue)
  lazy val contentEncodinghHdr: Option[String] = get(CONTENT_ENCODING).map(_.value)
  lazy val contentLengthHdr: Option[Long] = get(CONTENT_LENGTH).flatMap(_.toNumber)
  lazy val dateHdr: Option[HttpDateTimeInstant] = get(DATE).flatMap(_.toDate)
  lazy val etagHdr: Option[EntityTag] = get(ETAG).flatMap(_.toEntityTag)
  lazy val expiresHdr: Option[HttpDateTimeInstant] = get(EXPIRES).flatMap(_.toDate)
  lazy val lastModifiedHdr: Option[HttpDateTimeInstant] = get(LAST_MODIFIED).flatMap(_.toDate)
  lazy val locationHdr: Option[String] = get(LOCATION).map(_.value)

  override def toString() = list.mkString("[", "; ", "]")
}

//---------------------------------------------------------------------------------------------------------------------

/**
 * Provides support for creating `Headers` instances.
 */
object Headers {
  /** Constructs a new Headers instance from a list of headers. */
  def apply(headers: Header*): Headers = new Headers(headers.toList)

  /**
   * Constructs a new Headers instance from a map of strings. Bear in mind that the map keys are case-insensitive,
   * but you are advised to stick to the canonical capitalisation, which is as given by HeaderName.
   */
  def apply(map: Map[String, String]): Headers = new Headers(map.map(kv => HeaderName(kv._1) -> kv._2).toList)

  @deprecated("Use Empty", "Since v.0.21.5")
  val empty = Headers(Nil)

  val Empty = Headers(Nil)

  implicit def createHeaders(list: List[Header]): Headers = new Headers(list)

  implicit def stringToHeaderName(hn: String) = HeaderName(hn)
}