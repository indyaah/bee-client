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

import uk.co.bigbeeconsultants.http.header.{CookieJar, Headers, MediaType}
import uk.co.bigbeeconsultants.http.request.Request

/**
 * Represents a HTTP response. This is essentially immutable, although the implementation of
 * the enclosed response body may vary.
 *
 * Note that the body may represent one of three cases: in the two obvious cases,
 * there was real data supplied by the server, that may or may not have been empty. The third case is a special
 * case in which the body is empty because the HTTP standard does not permit a body under those circumstances.
 * This is true for HEAD requests and all those status codes such as 204 or 304 for which the status bodyAllowed
 * field is false.
 *
 * A response may sometimes indicate a redirect. The lazy field `redirectTo` is computed with the new request
 */
case class Response(request: Request,
                    status: Status,
                    body: ResponseBody,
                    headers: Headers,
                    cookies: Option[CookieJar])

object Response {
  /** Constructs a response instance containing a string body. */
  def apply(request: Request, status: Status, contentType: MediaType, bodyText: String,
            headers: Headers = Headers.empty, cookies: Option[CookieJar] = None) = {
    new Response(request, status, new StringResponseBody(contentType, bodyText), headers, cookies)
  }
}
