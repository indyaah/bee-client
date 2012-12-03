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

import auth.CredentialSuite
import java.net.Proxy
import request._
import javax.net.ssl.{SSLSocketFactory, HostnameVerifier}

/**
 * Specifies configuration options that will be used across many requests or for a particular request.
 *
 * @param connectTimeout sets the connection timeout in milliseconds (2000)
 * @param readTimeout sets the read timeout in milliseconds (5000)
 * @param followRedirects enabled automatic following of redirects (true)
 * @param maxRedirects sets the maximum times a cyclic redirect will be followed (20)
 * @param useCaches enables the use of content caches (true)
 * @param keepAlive when true, persistent keep-alive HTTP1.1 connections are used (true)
 * @param userAgentString sets the string that is used to identify this client to the server
 * @param proxy supplies the proxy configuration (NO_PROXY).
 *              See http://docs.oracle.com/javase/6/docs/api/java/net/Proxy.html
 * @param credentials provides a credentials source (empty) -- experimental
 * @param preRequests provides the pre-request handlers to configure headers etc on every request using
 *                    Config.standardSetup, i.e.
 *                    (List([[uk.co.bigbeeconsultants.http.request.AutomaticHostHeader]],
 *                    [[uk.co.bigbeeconsultants.http.request.DefaultRequestHeaders]],
 *                    [[uk.co.bigbeeconsultants.http.request.ConnectionControl]],
 *                    [[uk.co.bigbeeconsultants.http.request.UserAgentString]]))
 * @param hostnameVerifier an optional SSL hostname verifier to be used on all requests. This is similar to
 *                         setting the global state via HttpsURLConnection.setDefaultHostnameVerifier, except
 *                         that, this way, it is possible to configure multiple clients each with their own
 *                         hostname verifier.
 * @param sslSocketFactory an optional SSL socket factory to be used on all requests. This is similar to
 *                         setting the global state via HttpsURLConnection.setDefaultSSLSocketFactory, except
 *                         that, this way, it is possible to configure multiple clients each with their own
 *                         socket factory.
 */
case class Config(connectTimeout: Int = 2000,
                  readTimeout: Int = 5000,
                  followRedirects: Boolean = true,
                  maxRedirects: Int = 20,
                  useCaches: Boolean = true,
                  keepAlive: Boolean = true,
                  userAgentString: Option[String] = None,
                  proxy: Proxy = Proxy.NO_PROXY,
                  credentials: CredentialSuite = CredentialSuite.empty,
                  preRequests: List[PreRequest] = Config.standardSetup,
                  hostnameVerifier: Option[HostnameVerifier] = None,
                  sslSocketFactory: Option[SSLSocketFactory] = None) {

  require(maxRedirects > 1, maxRedirects + ": too few maxRedirects")
}

object Config {
  /**
   * Lists the setup steps applied to each request.
   * (List([[uk.co.bigbeeconsultants.http.request.AutomaticHostHeader]],
   * [[uk.co.bigbeeconsultants.http.request.DefaultRequestHeaders]],
   * [[uk.co.bigbeeconsultants.http.request.ConnectionControl]],
   * [[uk.co.bigbeeconsultants.http.request.UserAgentString]]))
   */
  val standardSetup = List(AutomaticHostHeader, DefaultRequestHeaders, ConnectionControl, UserAgentString)
}