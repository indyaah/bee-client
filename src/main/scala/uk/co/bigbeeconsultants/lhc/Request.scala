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

import java.net.URL

case class KeyVal(key: String, value: String)

case class Body(mediaType: MediaType, bytes: Array[Byte], data: List[KeyVal] = Nil)

object Body {
  def apply(mediaType: MediaType, string: String): Body = new Body(mediaType, string.getBytes(mediaType.charsetOrElse(Http.defaultCharset)), Nil)
  def apply(mediaType: MediaType, data: List[KeyVal]): Body = new Body(mediaType, Array(), data)
}

/**
 * Represents the requirements for an HTTP request. Immutable.
 */
case class Request(method: String, url: URL, body: Option[Body] = None)

object Request {
  val DELETE = "DELETE"
  val GET = "GET"
  val HEAD = "HEAD"
  val POST = "POST"
  val PUT = "PUT"
  val TRACE = "TRACE"

  def delete(url: URL, body: Option[Body] = None): Request = Request(DELETE, url, body)
  def get(url: URL, body: Option[Body] = None): Request = Request(GET, url, body)
  def head(url: URL, body: Option[Body] = None): Request = Request(HEAD, url, body)
  def post(url: URL, body: Option[Body] = None): Request = Request(POST, url, body)
  def put(url: URL, body: Option[Body] = None): Request = Request(PUT, url, body)
  def trace(url: URL, body: Option[Body] = None): Request = Request(TRACE, url, body)
}
