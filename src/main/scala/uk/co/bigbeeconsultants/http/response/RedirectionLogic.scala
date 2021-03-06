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

import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.request.Request._
import uk.co.bigbeeconsultants.http.header.{CookieJar, Headers}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.{Config, HttpClient}
import java.net.URL

private[http] object RedirectionLogic {

  def doExecute(httpClient: HttpClient, request: Request, responseBuilder: ResponseBuilder, config: Config): Int = {
    var nextRequest: Option[Request] = Some(request)
    var remainingTries = if (config.followRedirects) config.maxRedirects else 1
    while (remainingTries > 0 && nextRequest.isDefined) {
      remainingTries -= 1
      nextRequest = httpClient.doExecute(nextRequest.get, responseBuilder, config)
    }

    if (config.followRedirects && nextRequest.isDefined)
      throw new IllegalStateException("The server is not redirecting correctly. Maximum redirects (" +
        config.maxRedirects + ") was exceeded for " + request)

    remainingTries
  }

  /**
   * If the server indicated a redirection is required, this method returns the new
   * request object to be used, otherwise `None`.
   */
  def determineRedirect(config: Config, request: Request, status: Status,
                        responseHeaders: Headers, responseCookies: Option[CookieJar]): Option[Request] = {
    if (!config.followRedirects) None

    else status.code match {
      case 301 | 307 =>
        // First case: No change of method (302 ought to be here but cannot)
        val location = responseHeaders.locationHdr
        if (location.isEmpty) None
        else
          Some(Request(method = request.method,
            url = locationToURL(request, location.get),
            body = request.body,
            headers = request.headers,
            cookies = responseCookies))

      case 302 | 303 =>
        // Second case: switch to GET method (302 is de-facto only)
        val location = responseHeaders.locationHdr
        if (location.isEmpty) None
        else
          Some(Request(method = GET,
            url = locationToURL(request, location.get),
            body = None,
            headers = request.headers,
            cookies = responseCookies))

      case _ => None
    }
  }

  private def locationToURL(request: Request, location: String) = {
    val loc = if (location.startsWith("http://") || location.startsWith("https://")) {
      location
    } else {
      request.href.endpoint.get.toString + location
    }
    new URL(loc)
  }
}
