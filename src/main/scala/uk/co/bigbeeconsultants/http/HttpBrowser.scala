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

import auth._
import header._
import java.io.IOException
import request.Request
import response._
import uk.co.bigbeeconsultants.http.cache.{CookieCache, AuthCache, ContentCache, CacheConfig}

/**
 * Provides an alternative to [[uk.co.bigbeeconsultants.http.HttpClient]]
 * in which every request carries an outbound cookie jar and every
 * inbound response potentially provides a modified cookie jar. A series of requests will therefore behave like
 * a normal web-browser by aggregating cookies from the server and returning them back in subsequent requests.
 *
 * Automatic authentication is also supported.
 *
 * Concurrent requests with an instance of this class are permitted. But when concurrent requests are made,
 * note that there may be race conditions that may or may not cause some cookie updates to be overwritten,
 * depending on the server behaviour. Cookie jars themselves are immutable so will be in either of two states:
 * the original state or the updated state.
 */
class HttpBrowser(commonConfig: Config = Config(),
                  initialCookieJar: CookieJar = CookieJar.Empty,
                  credentialSuite: CredentialSuite = CredentialSuite.Empty,
                  cacheConfig: CacheConfig = CacheConfig()) extends Http(commonConfig) {

  private val coreHttpClient = new HttpClient(commonConfig)
  private val cookieCache = new CookieCache(coreHttpClient, initialCookieJar)
  private val authCache = new AuthCache(cookieCache, credentialSuite)
  private val httpClient = if (cacheConfig.enabled) new ContentCache(authCache, cacheConfig) else authCache

  /**
   * Gets the current state of the cookie jar. If multiple threads are executing, this will return a snapshot
   * of the state at some instant.
   */
  def cookies = cookieCache.cookies

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
  def execute(request: Request, responseBuilder: ResponseBuilder, config: Config = commonConfig) {
    httpClient.execute(request, responseBuilder, config)
  }
}
