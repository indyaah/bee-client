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

package uk.co.bigbeeconsultants.http

import header.MediaType
import java.net.{URL, MalformedURLException}
import url.Domain

object `package` {
  @throws(classOf[MalformedURLException])
  implicit def toURL(url: String) = new URL(url)

  implicit def toDomain(domain: String) = new Domain(domain)

  /** Instances of this type are functions that mutate a string in some way. */
  type TextFilter = (String) => String

  /** A text filter that simply returns the source text unchanged. */
  val NoChangeTextFilter: TextFilter = (x) => x

  /** Instances of this type are predicates that indicate which media types will be included in some operation. */
  type MediaFilter = (MediaType) => Boolean

  /** A predicate that indicates all media types that are known to be textual. */
  val AllTextualMediaTypes: MediaFilter = (mt) => mt.isTextual

  /** A predicate that returns false for all media types. */
  val NoMediaTypes: MediaFilter = (mt) => false
}
