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
import uk.co.bigbeeconsultants.http.response.{ResponseBuilder, Response}
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.header.WarningValue
import uk.co.bigbeeconsultants.http.util.HttpUtil._
import uk.co.bigbeeconsultants.http.{HttpExecutor, Config}

/**
 * Holds an HTTP content cache. Outbound requests are checked using `lookup`, which either returns a cached response
 * or provides an altered request to pass on to the origin server. All responses are offered to the cache via the
 * `store` method.
 *
 * The cache is *not* persistent: every time the HTTP client is started, any cache will start off empty.
 */
class ContentCache(httpClient: HttpExecutor,
                   cacheConfig: CacheConfig = CacheConfig()) extends HttpExecutor {
  require(cacheConfig.enabled, "Don't use this class if the cache is disabled.")

  private val data = new CacheStore(cacheConfig.maxCachedContentSize, cacheConfig.lazyCleanup)

  def cacheSize = data.size

  def clearCache() {
    data.clear()
  }

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
    val cacheRecord = data.get(request.cacheKey)
    if (cacheRecord == null)
      cacheMiss(request, responseBuilder, config)
    else if (cacheRecord.isFresh)
      cacheHit(cacheRecord, responseBuilder)
    else
      staleSoRevalidate(cacheRecord, request, responseBuilder, config)
  }

  private def cacheMiss(request: Request, responseBuilder: ResponseBuilder, config: Config) {
    makeHttpRequest(request, None, responseBuilder, config)
  }

  private def cacheHit(cacheRecord: CacheRecord, responseBuilder: ResponseBuilder) {
    val age = (cacheRecord.currentAge / 1000).toString
    val modResponse = cacheRecord.response.copy(headers = cacheRecord.response.headers.set(AGE -> age))
    responseBuilder.setResponse(modResponse)
    // no HTTP request is made
  }

  private def staleSoRevalidate(cacheRecord: CacheRecord, request: Request, responseBuilder: ResponseBuilder, config: Config) {
    var modHeaders = request.headers
    if (cacheRecord.etagHeader.isDefined) {
      val etag = IF_NONE_MATCH -> quote(cacheRecord.etagHeader.get.opaqueTag)
      modHeaders = modHeaders set etag
    }
    if (cacheRecord.lastModifiedHeader.isDefined) {
      val lastModified = IF_MODIFIED_SINCE -> cacheRecord.lastModifiedHeader.get.toString
      modHeaders = modHeaders set lastModified
    }
    val revalidateRequest = request.copy(headers = modHeaders)
    // pass on the stale response in case it gets dropped from the cache whilst the revalidation request is executing,
    makeHttpRequest(revalidateRequest, Some(cacheRecord.response), responseBuilder, config)
  }

  private def makeHttpRequest(request: Request,
                              staleResponse: Option[Response],
                              responseBuilder: ResponseBuilder, config: Config) {

    httpClient.execute(request, responseBuilder, config)

    if (responseBuilder.response.isDefined) {
      val primaryResponse = responseBuilder.response.get
      val updatedResponse = store(primaryResponse)
      if (updatedResponse.isDefined) {
        responseBuilder.setResponse(updatedResponse.get) // normal completion
      }
      // else edge case due to cache race during 304 refresh, so use the stale response from earlier
    } else if (staleResponse.isDefined) {
      responseBuilder.setResponse(staleResponse.get) // edge case
    }
  }

  private def store(response: Response): Option[Response] = {
    response.status.code match {
      case 206 => // partial content not supported
        Some(response)

      case 200 | 203 | 300 | 301 | 410 =>
        offerToCacheIfWorthIt(response)

      case 404 if cacheConfig.assume404Age > 0 =>
        val age = AGE -> cacheConfig.assume404Age
        val warning = WARNING -> WarningValue(110, "", "Stale content")
        val modHeaders = response.headers.set(age).set(warning)
        offerToCache(response.copy(headers = modHeaders))

      case 304 =>
        val cacheRecord = data.get(response.request.cacheKey)
        if (cacheRecord == null)
          None // expired and then deleted
        else {
          val oldResponse = cacheRecord.response
          var modHeaders = oldResponse.headers
          for (h <- response.headers) {
            modHeaders = modHeaders.set(h)
          }
          val newResponse = oldResponse.copy(headers = modHeaders)
          offerToCache(newResponse)
        }

      case _ =>
        controlledOfferToCache(response)
    }
  }

  private def offerToCacheIfWorthIt(response: Response) = {
    if (isWorthCaching(response) && isCacheable(response) && isNotPrevented(response))
      data.put(CacheRecord(response))
    Some(response)
  }

  private def offerToCache(response: Response) = {
    if (isCacheable(response) && isNotPrevented(response))
      data.put(CacheRecord(response))
    Some(response)
  }

  private def controlledOfferToCache(response: Response) = {
    if (isCacheable(response) && isNotPrevented(response))
      data.putIfControlled(CacheRecord(response))
    Some(response)
  }

  private def isNotPrevented(response: Response) = {
    val cacheControl = response.headers.cacheControlHdr
    if (cacheControl.isDefined)
      cacheControl.get.label match {
        case "no-cache" | "no-store" => false
        case _ => true
      } else true
  }

  private def isCacheable(response: Response) =
    (response.request.method == Request.GET || response.request.method == Request.HEAD) && response.body.isBuffered

  private def isWorthCaching(response: Response) = {
//    val expiresHdr = response.headers.expiresHdr
    val contentLengthHdr = response.headers.contentLengthHdr
    if (contentLengthHdr.isDefined)
      contentLengthHdr.get >= cacheConfig.minContentLength
    else
      response.body.contentLength >= cacheConfig.minContentLength
  }
}
