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

package uk.co.bigbeeconsultants.lhc.request

import uk.co.bigbeeconsultants.lhc.response.{Response, Status}


/**
 * Specifies configuration options that will be used across many requests.
 */
case class Config(connectTimeout: Int = 2000,
                  readTimeout: Int = 2000,
                  followRedirects: Boolean = true,
                  useCaches: Boolean = true,
                  sendHostHeader: Boolean = true)

object Config {
  /**
   * The default request configuration has two-second timeouts, follows redirects, and
   * sends the host header.
   */
  val default = new Config
}

class RequestException(val request: Request, val status: Status, val response: Option[Response], cause: Option[Exception])
  extends RuntimeException(cause orNull) {

  override def getMessage: String = {
    "%s %s\n  %d %s".format(request.method, request.url, status.code, status.message)
  }
}
