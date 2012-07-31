Light HTTP1.1 Client
====================

This API is a wrapper around the standard java.net.URL capability for
making HTTP requests. It has the following features:

*   Simple to use - provides easy programmatic HTTP client calls, handling the necessary
    headers and body data.

*   Light-weight - not much code; doesn't get in the way.

*   Fast performance and low memory footprint.

*   Easy handling of headers - both for requests and responses; both with simple values and
    with complex structure.

*   Flexible and efficient handling of content (entity) bodies, along with the media type and
    character encoding. UTF-8 is used as the default character encoding.

*   Request entity bodies can be streamed in. Response entity bodies can be streamed out.

*   Complete implementation: *all* HTTP methods and headers are supported.

*   Standards-compliance is better than HttpURLConnection.

*   HTTPS is supported - _as yet untested but based upon javax.net.ssl.HttpsURLConnection_

*   Cookies are supported. They are held in immutable cookie jars gleaned from response headers
    (or created programmatically) and then sent back with subsequent requests.

*   Full awareness of proxies.

*   Arbitrary multi-threading is possible because all shared state is held in immutable values.

*   No 'static' variables are used, so multiple configurations can co-exist within a JVM.

*   Tested against a range of servers: Apache2, Nginx, Lighttpd, Cherokee, Tomcat7.

*   The number of external dependencies is minimised (currently: slf4j, slf4s, servlet-api).

It is written in Scala but may be called (a lot less easily) from Java also.

Known Bugs
----------

*   See the [issue tracker](https://bitbucket.org/rickb777/lighthttpclient/issues?status=new&status=open "BitBucket IssueTracker for LightHttpClient")

Future Plans
------------

The following features are not supported yet, but are under consideration
for future versions.

*   Configurable content caching.

*   Proxy support enhanced to support conditional switching.

*   Tested against a wider range of servers.

Footnotes
---------

* Versions are numbered using the [SemVer](http://semver.org/) pattern.
