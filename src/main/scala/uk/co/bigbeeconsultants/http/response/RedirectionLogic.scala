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

  def doExecute(httpClient: HttpClient, request: Request, responseBuilder: ResponseBuilder): Int = {
    var nextRequest: Option[Request] = Some(request)
    var remainingTries = if (httpClient.config.followRedirects) httpClient.config.maxRedirects else 1
    while (remainingTries > 0 && nextRequest.isDefined) {
      remainingTries -= 1
      nextRequest = httpClient.doExecute(nextRequest.get, responseBuilder)
    }

    if (httpClient.config.followRedirects && nextRequest.isDefined)
      throw new IllegalStateException("The server is not redirecting correctly. Maximum redirects (" +
        httpClient.config.maxRedirects + ") was exceeded for " + request)

    remainingTries
  }

  /**
   * If the server indicated a redirection is required, this method returns the new
   * request object to be used, otherwise `None`.
   */
  def determineRedirect(config: Config, request: Request, status: Status,
                        responseHeaders: Headers, responseCookies: Option[CookieJar]): Option[Request] = {
    if (!config.followRedirects) None

    else if (status.code == 301 || status.code == 307) {
      // First case: No change of method (302 ought to be here but cannot)
      val location = responseHeaders.get(LOCATION)
      if (location.isEmpty) None
      else Some(Request(method = request.method,
        url = locationToURL(request, location.get.value),
        body = request.body,
        headers = request.headers,
        cookies = responseCookies))

    } else if (status.code == 302 || status.code == 303) {
      // Second case: switch to GET method (302 is de-facto only)
      val location = responseHeaders.get(LOCATION)
      if (location.isEmpty) None
      else Some(Request(method = GET,
        url = locationToURL(request, location.get.value),
        body = None,
        headers = request.headers,
        cookies = responseCookies))

    } else None
  }

  private def locationToURL(request: Request, location: String) = {
    val loc = if (location.startsWith("http://") || location.startsWith("https://")) {
      location
    } else {
      request.split.scheme + "://" + request.split.hostAndPort + location
    }
    new URL(loc)
  }
}
