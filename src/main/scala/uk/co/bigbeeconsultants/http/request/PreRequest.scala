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

package uk.co.bigbeeconsultants.http.request

import java.net.{URLConnection, HttpURLConnection}
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.util.IpUtil
import uk.co.bigbeeconsultants.http.header._
import uk.co.bigbeeconsultants.http.Config
import javax.net.ssl.{HostnameVerifier, HttpsURLConnection, SSLSocketFactory}
import com.weiglewilczek.slf4s.Logging


/**
 * Provides the interface for specifying pre-request actions on each request. A list of instances is supplied
 * to [[uk.co.bigbeeconsultants.http.Config]].
 */
trait PreRequest extends Logging {
  def process(request: Request, httpURLConnection: HttpURLConnection, config: Config)

  final def setHeader(urlConnection: URLConnection, header: Header) {
    urlConnection.setRequestProperty(header.name, header.value)
    logger.debug({
      header.toString
    })
  }
}


/**
 * Sets the "Connection: close" header if not a keep-alive connection.
 */
object ConnectionControl extends PreRequest {
  override def process(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    if (!config.keepAlive) {
      setHeader(httpURLConnection, ExpertHeaderName.CONNECTION -> "close")
    }
  }
}


/**
 * Sets the "Connection: close" header if not a keep-alive connection.
 */
object UserAgentString extends PreRequest {
  override def process(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    if (config.userAgentString.isDefined) {
      setHeader(httpURLConnection, RequestHeaderName.USER_AGENT -> config.userAgentString.get)
    }
  }
}


/**
 * Automatically sends the Host header with every request made to a named host. This excludes localhost and
 * IP addresses.
 */
object AutomaticHostHeader extends PreRequest {
  override def process(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    val host = request.url.getHost
    if (host != "localhost" && !IpUtil.isIpAddressSyntax(host)) {
      setHeader(httpURLConnection, RequestHeaderName.HOST -> host)
    }
  }
}


/**
 * Sends the following request headers with every request.
 * Accept: * / *
 * Accept-Encoding: gzip
 * Accept-Charset: UTF-8, *;q=.1
 */
object DefaultRequestHeaders extends PreRequest {
  val defaultRequestHeaders = Headers(
    ACCEPT -> "*/*",
    ACCEPT_ENCODING -> HttpClient.GZIP,
    ACCEPT_CHARSET -> (HttpClient.UTF8 + ",*;q=.1")
  )

  override def process(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    defaultRequestHeaders.foreach(setHeader(httpURLConnection, _))
  }
}


/**
 * Injects a particular SSL Socket Factory into every HTTPS request using it.
 */
class SSLSocketFactoryInjecter(sslSocketFactory: SSLSocketFactory) extends PreRequest {
  override def process(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    if (httpURLConnection.isInstanceOf[HttpsURLConnection]) {
      val httpsConnection = httpURLConnection.asInstanceOf[HttpsURLConnection]
      httpsConnection.setSSLSocketFactory(sslSocketFactory)
    }
  }
}


/**
 * Injects a particular hostname verifier into every HTTPS request using it.
 */
class HostnameVerifierInjecter(hostnameVerifier: HostnameVerifier) extends PreRequest {
  override def process(request: Request, httpURLConnection: HttpURLConnection, config: Config) {
    if (httpURLConnection.isInstanceOf[HttpsURLConnection]) {
      val httpsConnection = httpURLConnection.asInstanceOf[HttpsURLConnection]
      httpsConnection.setHostnameVerifier(hostnameVerifier)
    }
  }
}


