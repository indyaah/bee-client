Bee-Client - A Lightweight Scala HTTP Client
============================================

* **Bee Client is a Scala API that wraps the standard `java.net.URL` capability for making HTTP requests more easily.**
* **...Much more easily!**

It is written in Scala but may be called (a lot less easily) from Java also.

Simply create an `HttpClient` instance and get / post / whatever your requests.

    val httpClient = new HttpClient
    val response: Response = httpClient.get("http://www.google.com/")
    println(response.status)
    println(response.body.asString)

More examples follow in [the tutorial](http://www.bigbeeconsultants.co.uk/content/bee-client/basics).

## Features ##

* **Simple** to use - provides easy programmatic HTTP client calls, handling the necessary headers and body data.
* **Light-weight** - not much code; doesn't get in the way.
* **Fast** performance and low memory footprint.
* **Easy** handling of headers - both for requests and responses; both with simple values and with complex structure.
* Flexible and efficient handling of content (entity) bodies, along with the media type and character encoding. UTF-8 is used as the default character encoding.
* Request entity bodies can be **streamed** in. Response entity bodies can be **streamed** out.
* **Complete** implementation: *all* HTTP methods and headers are supported.
* **Standards-compliance** builds on `HttpURLConnection` and in some ways improves on it.
* **HTTPS** is supported (based upon `javax.net.ssl.HttpsURLConnection`)
* **Cookies** are supported. They are held in immutable cookie jars gleaned from response headers (or created programmatically) and then sent back with subsequent requests.
* **Automatic Redirection** preserves cookies (unlike using `HttpURLConnection` directly).
* Full awareness of **proxies**.
* Arbitrary **multi-threading** is possible because all shared state is held in immutable values.
* No additional threads are imposed - you decide what you need.
* No 'static' variables are used, so multiple configurations can co-exist within a JVM.
* **Tested** against a range of servers: Apache2, Nginx, Lighttpd, Cherokee, Tomcat7.
* The number of external dependencies is minimised (currently: slf4j, servlet-api).

## Known Bugs ##

*   See the [issue tracker](https://bitbucket.org/rickb777/bee-client/issues?status=new&status=open "BitBucket IssueTracker for Bee-Client")

## Future Plans ##

Several new features are under consideration for future versions.

See https://bitbucket.org/rickb777/bee-client/issues?status=new&status=open

[![Build Status](https://drone.io/bitbucket.org/rickb777/bee-client/status.png)](https://drone.io/bitbucket.org/rickb777/bee-client/latest)

