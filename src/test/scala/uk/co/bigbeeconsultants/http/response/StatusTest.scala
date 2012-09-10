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

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.bigbeeconsultants.http.request._
import uk.co.bigbeeconsultants.http.header.{Domain, Cookie, CookieJar, Headers}
import uk.co.bigbeeconsultants.http.header.MediaType._
import uk.co.bigbeeconsultants.http.header.HeaderName._

class StatusTest extends FunSuite with ShouldMatchers {

  test("isInformational") {
    assert(Status.S100_Continue.isInformational)
    assert(!Status.S200_OK.isInformational)
  }

  test("isSuccess") {
    assert(!Status.S100_Continue.isSuccess)
    assert(Status.S200_OK.isSuccess)
  }

  test("isRedirection") {
    assert(!Status.S100_Continue.isRedirection)
    assert(Status.S301_MovedPermanently.isRedirection)
  }

  test("isClientError") {
    assert(!Status.S100_Continue.isClientError)
    assert(Status.S400_BadRequest.isClientError)
  }

  test("isServerError") {
    assert(!Status.S100_Continue.isServerError)
    assert(Status.S500_InternalServerError.isServerError)
  }

  test("isBodyAllowed") {
    assert(!Status.S100_Continue.isBodyAllowed)
    assert(!Status.S204_NoContent.isBodyAllowed)
    assert(!Status.S205_ResetContent.isBodyAllowed)
    assert(!Status.S304_NotModified.isBodyAllowed)
    assert(Status.S200_OK.isBodyAllowed)
    assert(Status.S500_InternalServerError.isBodyAllowed)
  }
}