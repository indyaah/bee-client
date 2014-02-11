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
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.auth.CredentialSuite
import uk.co.bigbeeconsultants.http.auth.AuthenticationRegistry
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.ResponseBuilder
import uk.co.bigbeeconsultants.http.response.Status

class AuthCache(nextHttpClient: HttpExecutor,
                credentialSuite: CredentialSuite = CredentialSuite.Empty) extends HttpExecutor {

  private val authenticationRegistry = new AuthenticationRegistry(credentialSuite, true)

  /**
   * Makes an arbitrary request using a response builder. After this call, the response builder will provide the
   * response.
   * @param request the request
   * @param responseBuilder the response factory, e.g. new BufferedResponseBuilder
   * @param config the particular configuration being used for this request; defaults to the commonConfiguration
   *               supplied to this instance of HttpClient
   * @throws IOException (or ConnectException subclass) if an IO exception occurred
   * @throws IllegalStateException if the maximum redirects threshold was exceeded
   */
  @throws(classOf[IOException])
  @throws(classOf[IllegalStateException])
  def execute(request: Request, responseBuilder: ResponseBuilder, config: Config) {

    val realmMappings = authenticationRegistry.findRealmMappings(request.href)
    var authHeader = authenticationRegistry.findKnownAuthHeaderFromMappings(request, realmMappings)

    var remainingRetries = 5
    while (remainingRetries > 0) {

      val requestWithAuth = if (authHeader.isDefined) request + authHeader.get else request
      nextHttpClient.execute(requestWithAuth, responseBuilder, config)

      responseBuilder.response match {
        case Some(resp) if resp.status.code == Status.S401_Unauthorized.code =>
          // authentication was missing or failed - try again
          authHeader = authenticationRegistry.processResponse(resp, realmMappings)
          if (authHeader.isDefined) remainingRetries -= 1 else remainingRetries = 0

        case Some(resp) if resp.status.code < Status.S400_BadRequest.code =>
          // authentication succeeded or is not used
          // TODO the cache should be updated
          remainingRetries = 0

        case _ =>
          // responseBuilder failed to capture a response or authentication succeeded or is not used
          remainingRetries = 0
      }
    }
  }
}
