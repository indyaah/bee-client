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

/**
 * Provides convenience methods to configure global HTTP properties. Unlike the Config parameter
 * passed into HttpClient, these global settings affect everything that shares the JVM, so they should
 * be used carefully.
 *
 * See http://docs.oracle.com/javase/6/docs/technotes/guides/net/properties.html
 *
 * There are some properties that are not supported here because a better alternative is to configure your
 * HttpClient instances with the commonRequestHeaders parameter to achieve exactly the same effect without
 * requiring a global setting. These are as follows:
 *
 * For "http.keepalive" (default: true), include "Connection: close".
 *
 * For "http.agent", include "User-Agent: your-string".
 *
 * For "http.proxyHost" etc, use the Proxy parameter to HttpClient constructor.
 */
object HttpGlobalSettings {

  def maxConnections = System.getProperty("http.maxConnections", "5").toInt

  /**
   * If HTTP keep-alive is enabled, this value is the number of idle connections that will be simultaneously kept
   * alive, per-destination. Default: 5.
   */
  def maxConnections_=(n: Int) {
    System.setProperty("http.maxConnections", n.toString)
  }

// Since Java7
//  def maxRedirects = System.getProperty("http.maxRedirects", "20").toInt
//
//  def maxRedirects_=(n: Int) {
//    System.setProperty("http.maxRedirects", n.toString)
//  }
}
