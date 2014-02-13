package uk.co.bigbeeconsultants.http.cache

import uk.co.bigbeeconsultants.http.util.Bytes._

/**
 * Holds an HTTP content cache. Outbound requests are checked using `lookup`, which either returns a cached response
 * or provides an altered request to pass on to the origin server. All responses are offered to the cache via the
 * `store` method.
 *
 * The cache is *not* persistent: every time the HTTP client is started, any cache will start off empty.
 *
 * @param enabled entirely enables or disables caching; enabled by default
 * @param maxCachedContentSize set an upper limit on the size of the cache, in terms of the total number of
 *                             bytes in the unencoded content lengths of all the cached responses. The default
 *                             value is 10,000,000 bytes.
 * @param minContentLength threshold below which responses skip the cache. There is a trade-off of the processing
 *                         needed to maintain cache entries vs the time saved by not fetching content. For small
 *                         messages, the overhead will normally exceed the benefit because the whole HTTP message
 *                         will fit into a single IP packet, regardless of whether it is a 200 response or a 304
 *                         not modified. For larger messages, the benefit is clearly greater than
 *                         the cost of caching. By default, messages of 1000 bytes or more are cached.
 *                         Note that the `ageThreshold` also applies.
 * @param ageThreshold age threshold (seconds) below which the `minContentLength` threshold applies. Responses with
 *                     a freshness lifetime beyond this age will always be cached, even those smaller than
 *                     the `minContentLength`. The freshness lifetime of a resposne is defined by rfc2616 section
 *                     13.2.4 and is based on the Max-Age header if present, or the Expires header if present, or
 *                     is zero.
 * @param assume404Age provides optional caching for 404 responses - these are not normally cached but can therefore
 *                     be a pain. Provide an assumed age (in seconds) and all 404 responses will be stored in the
 *                     cache as if the response had contained that age in a header. Zero disables this feature and is
 *                     the default value.
 * @param lazyCleanup controls whether a separate thread is used for freeing stale cache records. When false,
 *                    the cache is checked for stale content every time data is stored, which ensures
 *                    always-consistent behaviour. When true, a separate background thread is created and this
 *                    implements an eventually-consistent model. In this case, the synchronized blocks of
 *                    code are very short so there is very little dynamic coupling between concurrent requests.
 *                    The default setting is enabled.
 */
case class CacheConfig(enabled: Boolean = true,
                       maxCachedContentSize: Long = 10 * MiB,
                       minContentLength: Int = 1000,
                       ageThreshold: Int = 60,
                       assume404Age: Int = 0,
                       lazyCleanup: Boolean = true) {

  require(maxCachedContentSize > 0, "maxCachedContentSize must be greater than zero")
  require(minContentLength >= 0, "minContentLength must non-negative")
  require(ageThreshold >= 0, "ageThreshold must non-negative")
  require(assume404Age >= 0, "assume404Age must be non-negative")

  /** The age threshold in milliseconds. */
  lazy val ageThresholdMs = ageThreshold * 1000
}
