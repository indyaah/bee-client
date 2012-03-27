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

import java.net.URL
import uk.co.bigbeeconsultants.http.response.{ByteBufferResponseBodyFactory, ResponseBodyFactory}

/**
 * Represents the requirements for an HTTP request. Immutable.
 * Normally, this class will not be instantiated directly but via the Request object methods.
 */
final case class Request(method: String,
                         url: URL,
                         body: Option[RequestBody] = None,
                         responseBodyFactory: ResponseBodyFactory = ByteBufferResponseBodyFactory)

/**
 * Provides factory methods for creating request objects of various types. These include
 * <ul>
 *   <li>methods without an entity body: get, head, delete, trace</li>
 *   <li>method with an optional entity body: options</li>
 *   <li>methods requiring an entity body: post, put</li>
 * </ul>
 */
object Request {
  val DELETE = "DELETE"
  val GET = "GET"
  val HEAD = "HEAD"
  val OPTIONS = "OPTIONS"
  val POST = "POST"
  val PUT = "PUT"
  val TRACE = "TRACE"

  // methods without an entity body
  def get(url: URL): Request = Request (GET, url, None)

  def head(url: URL): Request = Request (HEAD, url, None)

  def delete(url: URL): Request = Request (DELETE, url, None)

  def trace(url: URL): Request = Request (TRACE, url, None)

  // method with an optional entity body
  def options(url: URL, body: Option[RequestBody] = None): Request = Request (OPTIONS, url, body)

  // methods requiring an entity body
  def post(url: URL, body: RequestBody): Request = Request (POST, url, Some (body))

  def put(url: URL, body: RequestBody): Request = Request (PUT, url, Some (body))
}
