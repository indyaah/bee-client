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

package uk.co.bigbeeconsultants.http.cache

import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import uk.co.bigbeeconsultants.http.{Config, HttpExecutor}
import uk.co.bigbeeconsultants.http.header.CookieJar
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.ResponseBuilder

class CookieCache(nextHttpClient: HttpExecutor,
                  initialCookieJar: CookieJar = CookieJar.Empty) extends HttpExecutor {

  private val cookieJar = new AtomicReference[CookieJar](initialCookieJar)

  /** Gets the current state of the cookie jar. */
  def cookies = cookieJar.get

  @throws(classOf[IOException])
  @throws(classOf[IllegalStateException])
  def execute(request: Request, responseBuilder: ResponseBuilder, config: Config) {

    val requestWithCookies = request using cookieJar.get

    nextHttpClient.execute(requestWithCookies, responseBuilder, config)

    if (responseBuilder.response.isDefined) {
      cookieJar.set(responseBuilder.response.get.cookies.get)
    }
  }
}
