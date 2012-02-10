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

package uk.co.bigbeeconsultants.lhc.response

import uk.co.bigbeeconsultants.lhc.header.Header

/**Expresses an HTTP status line. */
case class Status(code: Int, message: String)

/**
 * Lists the official status codes as defined in RFC2616.
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1
 * <p>
 * Note that java.net.HttpURLConnection has non-standard status codes
 * in some cases.
 */
object Status {
  val S1_CONTINUE = 100
  val S1_SWITCHING_PROTOCOLS = 101

  /*----- 2XX: success -----*/
  val S2_OK = 200
  val S2_CREATED = 201
  val S2_ACCEPTED = 202
  val S2_NOT_AUTHORITATIVE = 203
  val S2_NO_CONTENT = 204
  val S2_RESET = 205
  val S2_PARTIAL = 206

  /*----- 3XX: relocation/redirect -----*/
  val S3_MULT_CHOICE = 300
  val S3_MOVED_PERM = 301
  val S3_FOUND = 302
  val S3_SEE_OTHER = 303
  val S3_NOT_MODIFIED = 304
  val S3_USE_PROXY = 305
  val S3_MOVED_TEMP = 307

  /*----- 4XX: client error -----*/
  val S4_BAD_REQUEST = 400
  val S4_UNAUTHORIZED = 401
  val S4_PAYMENT_REQUIRED = 402
  val S4_FORBIDDEN = 403
  val S4_NOT_FOUND = 404
  val S4_BAD_METHOD = 405
  val S4_NOT_ACCEPTABLE = 406
  val S4_PROXY_AUTH = 407
  val S4_CLIENT_TIMEOUT = 408
  val S4_CONFLICT = 409
  val S4_GONE = 410
  val S4_LENGTH_REQUIRED = 411
  val S4_PRECON_FAILED = 412
  val S4_ENTITY_TOO_LARGE = 413
  val S4_REQ_URI_TOO_LONG = 414
  val S4_UNSUPPORTED_TYPE = 415
  val S4_RANGE_NOT_SATISFIABLE = 416
  val S4_EXPECTATION_FAILED = 417

  /*----- 5XX: server error -----*/
  val S5_INTERNAL_ERROR = 500
  val S5_NOT_IMPLEMENTED = 501
  val S5_BAD_GATEWAY = 502
  val S5_SERVICE_UNAVAILABLE = 503
  val S5_GATEWAY_TIMEOUT = 504
  val S5_UNSUPPORTED_VERSION = 505
}