Light HTTP1.1 Client
====================

This API is a wrapper around the standard java.net.URL capability for
making HTTP requests. It has the following features:

*   Simple to use - provides easy programmatic HTTP client calls, handling the necessary
    headers and body data.

*   Light-weight - not much code; doesn't get in the way.

*   Fast performance and low memory footprint.

*   Flexible and efficient handling of content (entity) bodies.

*   Easy handling of headers - both for requests and responses; both with simple values and
    with complex structure.

*   Complete implementation: *all* HTTP methods and headers are supported.

*   Standards-compliance is better than HttpURLConnection.

*   Cookies are supported. They are held in immutable cookie jars gleaned from response headers
    (or created programmatically) and then sent back with subsequent requests.

*   Full awareness of proxies.

*   Arbitrary multi-threading is possible because all shared state is held in immutable values.

*   No 'static' variables are used, so multiple configurations can co-exist within a JVM.

*   Tested against a range of servers.

It is written in Scala but may be called (a lot less easily) from Java also.

Future Plans
------------

The following features are not supported yet, but are under consideration
for future versions.

*   Gzip content encoding (doesn't work in the current version yet)

*   HTTP/1.1 keep-alive connections.

*   HTTP/1.1 chunked encoding.

*   Configurable content caching.

*   HTTPS support.

*   Proxy support enhanced to support conditional switching.

*   Tested against a very wide range of servers.

*   Support for range-valued headers.
