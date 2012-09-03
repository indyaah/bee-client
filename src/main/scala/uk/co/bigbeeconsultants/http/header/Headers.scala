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
 * Holds a list of headers.
 * <p>
 * Remember that header names are case-insensitive. The get and find methods take this into account.
 */
case class Headers(list: List[Header]) {

  def iterator = list.iterator

  def foreach[U](f: Header => U) { iterator.foreach(f) }

  def isEmpty = list.isEmpty

  def size = list.size

  def names: List[String] = list.map(_.name)

  /**
   * Tests whether a given header is present.
   * @param name the required header name. Uppercase or lowercase doesn't matter.
   */
  def contains(name: HeaderName) = list.find(_.name equalsIgnoreCase name.name).isDefined

  /**
   * Finds all the headers that have a given name.
   * @param name the required header name. Uppercase or lowercase doesn't matter.
   */
  def filter(name: String): List[Header] = list.filter(_.name equalsIgnoreCase name)

  /**
   * Finds the header that has a given name. If more than one match exists, only the first
   * will be returned.
   * @param name the required header name. Uppercase or lowercase doesn't matter.
   */
  def get(name: String): Option[Header] = list.find(_.name equalsIgnoreCase name)

  /**
   * Finds the header that has a given name. If none exists, an exception will be thrown.
   * If more than one match exists, only the first will be returned.
   * @param name the required header name. Uppercase or lowercase doesn't matter.
   */
  def apply(name: String): Header = list.find(_.name equalsIgnoreCase name).get

  def remove(name: HeaderName): Headers = {
    Headers(list.filterNot(_.name.equalsIgnoreCase(name.name)))
  }

  def -(name: HeaderName): Headers = {
    remove(name)
  }

  def add(newHeader: Header): Headers = {
    new Headers(newHeader :: list)
  }

  def +(newHeader: Header): Headers = {
    add(newHeader)
  }
}

object Headers {
  def apply(headers: Header*): Headers = new Headers(headers.toList)

  val empty = Headers(Nil)

  implicit def createHeaders(list: List[Header]): Headers = new Headers(list)
}