* branch:      switch-to-gradle
#### Thu, 12 Feb 2015
* Further test work done on CookieParserTest. Some gradle revisions.


* branch:      switch-to-gradle
#### Wed, 14 Jan 2015
* Gradle build is coming together.


* branch:      switch-to-gradle
#### Wed, 14 Jan 2015
* Broken build - transition to using symlinks


* branch:      switch-to-gradle
#### Wed, 14 Jan 2015
* Started new Gradle-based build system to unblock the SBT build hindrance log-jam.


#### Wed, 24 Dec 2014
* v0.29.0 ; sbt choice reverted to 0.13.5 because the newer version fails.


#### Sun, 21 Dec 2014
* v0.29.0 ; fixes bug #31


#### Sun, 21 Dec 2014
* Bugfix for issue #31 cookies with incomplete expires information are handled gracefully now.
* https://bitbucket.org/rickb777/bee-client/issue/31/cookie-parser-exception


#### Sun, 21 Dec 2014
* SBT gen-idea plugin dropped - doesn't play nicely with IntelliJ/J 14. SBT upped to v0.13.7.


#### Wed, 17 Sep 2014
* CacheVsStubTest stability issue reduced. SLF4J & Logback dependencies upped to 1.7.7 and 1.1.2.


#### Wed, 03 Sep 2014
* CacheStore has been switched to use a LinkedTransferQueue because it operates faster than LinkedBlockingQueue.
* CacheStoreTest now covers the case that had been missed: a limited queue length.


#### Wed, 03 Sep 2014
* Code tidied up (some imports were optimised).


#### Tue, 02 Sep 2014
* Housekeeping issues


#### Tue, 02 Sep 2014
* Small housekeeping items


#### Tue, 02 Sep 2014
* A little loosening up of some timing-based tests.
* These need rewriting using the predicatable Clock (or similar).


#### Tue, 02 Sep 2014
* Reverted the change concerning scalatest Matchers because backward compatibility is needed.


#### Tue, 02 Sep 2014
* v0.28.0 - Minor tweaks to tests and test support code.


* parent:      308:9ddd0462675b
* parent:      307:a6f62b514634
#### Tue, 02 Sep 2014
* v0.28.0 - Merged and revised.


* parent:      299:a42bc85cb3e9
#### Tue, 02 Sep 2014
* v0.28.0 - Upgraded to work with Scala 2.11, as well as 2.10 and 2.9.
* With thanks to https://bitbucket.org/codedefinition.


#### Thu, 28 Aug 2014
* test setup script tweaked.


#### Thu, 28 Aug 2014
* ninx test configuration tweaked (this is still fragile and needs replacing)


#### Tue, 15 Jul 2014
* plugins.sbt - changed as per pull-request #4


#### Tue, 15 Jul 2014
* AuthenticationRegistry - changed as per pull-request #4


#### Tue, 15 Jul 2014
* ResponseBuilderTest timeout relaxed based on experience.


#### Wed, 09 Jul 2014
* v0.27.2 - Reverted to v0.27.2 prior to next release.


#### Tue, 08 Jul 2014
* v0.28.0 - Support for Scala 2.11; some cache tweaks.


* parent:      289:96b1ed56ecf5
#### Tue, 10 Jun 2014
* v0.27.1 - sbt now 0.13.5. Some minor test changes.


* parent:      293:8e5c9aeaf3c1
* parent:      298:ad3d50548730
#### Tue, 02 Sep 2014
* Merged in codedefinition/bee-client1 (pull request #5)

* Updates for Scala 2.11 build and package v3.


#### Fri, 22 Aug 2014
* Added scala-actors, all compile, test fails due to AllExamples 3b and 3c catching ConnectException instead of UnknownHostException, since host names on those tests are incorrect


#### Mon, 14 Jul 2014
* Updating sbt to the latest version 0.13.5


#### Fri, 11 Jul 2014
* replacing for JavaConversions by JavaConverters for crossScalaVersions compilation compatability


#### Fri, 11 Jul 2014
* Replacing asConcurrentMap by mapAsScalaConcurrentMap. It was deprecated on 2.10 an removed on 2.11


* parent:      292:035199ef7e95
#### Fri, 11 Jul 2014
* Enabling scalatest version selection based on current scalaverion beeing used. Used for crossScalaVersions


#### Tue, 15 Jul 2014
* Replaced netstat with lsof so that it operates faster.


#### Thu, 10 Jul 2014
* Timing-related testing can be improved with the addition of a DI-based Clock class. This introduces JodaTime as a dependency


#### Tue, 17 Jun 2014
* v0.27.2 - ListValue has additional IgnorableCaseValue to make it easier to ignore the case of values (e.g. "gzip" in Accept-Encoding and Content-Encoding)


#### Tue, 10 Jun 2014
* v0.27.1 - More test code: exploring the keep-alive vs close connection handling.


#### Thu, 27 Feb 2014
* v0.27.1 - benchmark for large-file transfer speed is now working


#### Wed, 26 Feb 2014
* v0.27.1 - started on performance improvement works
* -- bugfix in ResponseBuilder diagnostic timing
* -- Duration.toString has improved rendering


#### Wed, 26 Feb 2014
* v0.27.1 - started on performance improvement works


#### Thu, 13 Feb 2014
* v0.27.0 - HTTP caching is now working


#### Wed, 12 Feb 2014
* v0.26.6 - HttpDateTimeInstant now parses timestamps with two-digit year.
* This is a bugfix (https://bitbucket.org/rickb777/bee-client/issue/24).


#### Wed, 12 Feb 2014
* v0.26.6 - Further caching work: CacheStore api now uses CacheRecord instances consistently.


#### Tue, 11 Feb 2014
* v0.26.6 - Some minor performance tweaks.


#### Tue, 11 Feb 2014
* v0.26.6 - ContentCacheTest tests extended.


#### Tue, 11 Feb 2014
* v0.26.6 - ContentCacheTest tests extended.


#### Tue, 11 Feb 2014
* v0.26.6
* - ContentCacheTest tests extended.


#### Tue, 11 Feb 2014
* v0.26.6
* - ContentCacheTest tests extended.


#### Tue, 11 Feb 2014
* v0.26.6
* - ContentCacheTest tests extended.


#### Tue, 11 Feb 2014
* v0.26.6
* - ContentCacheTest tests re-instated.


#### Tue, 11 Feb 2014
* v0.26.6
* - HttpBrowser function has been split into three separate parts - cookie caching, authentication caching and content caching.


#### Tue, 11 Feb 2014
* v0.26.6
* - HttpBrowser function has been split into three separate parts - cookie caching, authentication caching and content caching.


#### Mon, 10 Feb 2014
* v0.26.5
* - HttpBrowser caching arrangement has been altered. Original (pre v0.26) constructor for HttpBrowser has been re-instated, with the addition of a CacheConfig parameter. Caching will be delegated to CachingBrowser when it is enabled. This will be easier to test fully and have less impact on any existing code that uses HttpBrowser.


#### Mon, 10 Feb 2014
* v0.26.5
* - HttpBrowser caching arrangement has been altered. Original (pre v0.26) constructor for HttpBrowser has been re-instated, with the addition of a CacheConfig parameter. Caching will be delegated to CachingBrowser when it is enabled. This will be easier to test fully and have less impact on any existing code that uses HttpBrowser.


#### Thu, 06 Feb 2014
* v0.26.5
* - some tidy-up of header value parsing code


#### Thu, 06 Feb 2014
* v0.26.4 - mitigated the external effects of an API change that was in v0.26.3


#### Thu, 06 Feb 2014
* v0.26.4 - mitigated the external effects of an API change that was in v0.26.3


#### Thu, 06 Feb 2014
* v0.26.4 - mitigated the external effects of an API change that was in v0.26.3


#### Thu, 06 Feb 2014
* v0.26.3
* - resolved issue #22 - cookie expiry
* - resolved issue #23 - session cookies
* - cookies are now sorted in path order before being send with a request.
* - expired cookies are now deleted correctly.
* - all tests play nicely with JUnit as well as Scalatest - works better in some IDEs.


#### Wed, 05 Feb 2014
* Investigating Issue #22


#### Wed, 05 Feb 2014
* v0.26.2 - More testing of the caching code


#### Tue, 04 Feb 2014
* v0.26.2
* - Further development work on caching: CacheStore no longer has a singleton; etags now work.
* - References to Request.split (deprecated) have been fixed.
* - Headers.toString has been implemented.
* - Some unnecessary debug logging has been removed.
* - ResponseBuilder now captures the timer that gives the network time taken.
* - Better diagnostics are logged by HttpClient.


#### Tue, 04 Feb 2014
* v0.26.2 - Further development work on caching


#### Mon, 03 Feb 2014
* v0.26.2 - Further development work on caching


#### Mon, 03 Feb 2014
* v0.26.2
* - CacheKey is a little more efficient
* - CacheStore now has a lazy eventually-consistent cleanup of stale records via a single background thread


#### Sun, 02 Feb 2014
* v0.26.2
* - Cache is is now basically working. Edge cases remain to be completed.


#### Sat, 01 Feb 2014
* v0.26.2
* - Cache is now a trait. There are two implementations: NoOpCache and InMemoryCache
* - ResponseBuilder now has a setter to be used when returning a response from the cache
* - UnbufferedResponseBuilder can now capture 206 partial content as well as 200 OK responses
* - HttpBrowser now has the basics of caching wired in, although still not ready for production use.


#### Thu, 30 Jan 2014
* v0.26.2 - header value tidy-ups


#### Thu, 30 Jan 2014
* v0.26.2 - cache lookup partly done; entity tag now supports a list value


#### Wed, 29 Jan 2014
* v0.26.2 - new EntityTag header value


#### Wed, 29 Jan 2014
* v0.26.2 - more work done on caching classes: technique for specifying content size using bytes units.


#### Wed, 29 Jan 2014
* v0.26.2 - more work done on caching classes: optional 404 cached records get a warning now; new WarningValue factory constructor


#### Tue, 28 Jan 2014
* v0.26.2 - more work done on caching classes


#### Tue, 28 Jan 2014
* v0.26.2 - more work done on caching classes


#### Tue, 28 Jan 2014
* v0.26.2 - more work done on caching classes


#### Tue, 28 Jan 2014
* v0.26.2 - more work done on caching classes


#### Tue, 28 Jan 2014
* v0.26.1 - some extra methods have been added to HeaderName


#### Tue, 28 Jan 2014
* v0.26.0 - MediaType value field has been renamed mediaType. THIS IS A BREAKING CHANGE and call sites will have to be modified. The purpose is to fix a bug and make MediaType consistent with all other Value implementations in that the `value` field is the representation used in HTTP headers.


#### Tue, 28 Jan 2014
* v0.25.3
* - More Cache work done
* - CacheRecord was enhanced
* - CacheControlValue is now more complete


#### Mon, 27 Jan 2014
* v0.25.3 - CacheRecord was enhanced


#### Fri, 24 Jan 2014
* v0.25.3
* - new CacheControlValue


#### Fri, 24 Jan 2014
* v0.25.3
* - Seconds class is unwanted - deleted


#### Fri, 24 Jan 2014
* v0.25.3
* - Cache algorithm work done.
* - HttpDateTimeInstant reverts to using Long instead of Seconds


#### Thu, 23 Jan 2014
* v0.25.2 - HttpDateTimeInstant has been moved into the header sub-package and a new Seconds duration complements it.


#### Thu, 23 Jan 2014
* preparation for v0.26 - some new caching code


#### Wed, 22 Jan 2014
* v0.25.1 - header value classes have been slightly simplified and made more consistent with each other. There is a new WarningValue.


#### Wed, 22 Jan 2014
* v0.25.1 - header value classes have been slightly simplified and made more consistent with each other. There is a new WarningValue.


#### Wed, 22 Jan 2014
* v0.25.1 - servlet interfacing adapters have been deprecated. These will be removed in a future release; the source code will be made available to anyone who wishes to continue using them.


#### Wed, 22 Jan 2014
* v0.25
* - A simplified URLMapper now works.
* - HttpServletResponseAdapter processes textual responses differently and passes on a correct content-length now.


#### Thu, 14 Nov 2013
* v0.24.1 - Config now provides an easy way allow insecure SSL connections for development purposes.


#### Thu, 07 Nov 2013
* v0.24.0 - Config proxy default value is as per v0.23, i.e. NO_PROXY. This means that the -Dhttp.proxyXxxx properties will not work by default, but only when the proxy is explicity set to None. This ensures that a lesser-used feature doesn't impact the majority use case of direct connections.


#### Thu, 07 Nov 2013
* v0.24.0 - Config altered so that the proxy is optional. If no proxy is specified, the standard Java http.proxXxx settings control how proxying is handled. If NO_PROXY is specified, connections are always direct (which is slightly faster). Otherwise, a general or per-request proxy can be used as required.
* Note that Betamax has been removed from the dependencies; JavaStubServer is sufficient for stub-based testing (now at v0.12).


#### Tue, 05 Nov 2013
* Adjusts the Config parameter Proxy to an Option[Proxy]

* This makes it so that the default state does not override the proxy specified on the JVM command line

* Tests are included using Betamax, which provides the simple proxy functionality.


#### Mon, 21 Oct 2013
* v0.23.1 - Further work on equals and hashCode methods in RequestBody.


#### Mon, 21 Oct 2013
* v0.23.1 - Further work on equals and hashCode methods in RequestBody.


#### Sun, 20 Oct 2013
* v0.23.1 - RequestBody now implements equals and hashCode methods


#### Sun, 13 Oct 2013
* v0.23.0 - new AcceptValue exists for constructing well-formed Accept request headers easily.
* - CmmaListValue and SemicolonListValue apply methods have been renamed split because that's what they does.


#### Sun, 13 Oct 2013
* v0.23.0 - MediaType.contentType field has been renamed to mainType to reflect its purpose more correctly. It is possible to use `type` but this wasn't adopted because of the inconsistency this introduces into source code at usage sites, vs the subType field.
* - The value field is now deprecated; there will be a *breaking change* in the next release because this will be used for the representation value. This will become consistent with the Value trait, which now has a method called value.


#### Sat, 12 Oct 2013
* v0.22.8 - CookieJar.Empty and CredentialSuite.Empty replace the previous constants called 'empty'.


#### Tue, 10 Sep 2013
* v0.22.7 - Minor bug research. No substantial change.


#### Sat, 03 Aug 2013
* v0.22.6 - Href has new queryParts and queryMap methods


#### Sat, 03 Aug 2013
* v0.22.6 - Href has new queryParts and queryMap methods


#### Sat, 03 Aug 2013
* v0.22.6 - Path and Domain classes improved by addition of unapply extractor.


#### Thu, 25 Jul 2013
* v0.22.5 - Status class improved by addition of unapply extractor.


#### Mon, 22 Jul 2013
* v0.22.4 - Status class improved so that equality testing does the more intuitive thing of testing only the status code (i.e. any difference in the documentary message is ignored).


#### Thu, 11 Jul 2013
* v0.22.3 - byte-to-string decoding is now more intelligent also fixed in InputStreamResponseBody.


#### Thu, 11 Jul 2013
* v0.22.2 - byte-to-string decoding is now more intelligent. Any input data can be converted to string provided the decoder for the corresponding character set can handle the character sequence without exception. Any such exceptions are passed on.


#### Thu, 11 Jul 2013
* v0.22.1 - performance tweak converting ByteBuffer to Array[Byte]


#### Thu, 11 Jul 2013
* v0.22.0 - documentation and test code only


#### Thu, 11 Jul 2013
* v0.22.0 - ResponseBody implementations are now much more informative in the edge case of attempting to deal with binary data as if it were text. This is now not allowed and an exception is thrown if it is attempted.
* - MediaType now understands more fully which types are textual and which are binary.


#### Wed, 10 Jul 2013
* v0.21.6 - ByteBufferResponseBody is now a simpler case class; the complex construction has been moved to the factory methods in the companion object


#### Wed, 10 Jul 2013
* v0.21.5 - documentation


#### Wed, 10 Jul 2013
* v0.21.5 - revised ResponseBody implementations to be more flexible and orthogonal


#### Tue, 09 Jul 2013
* v0.21.5 - more documentation


#### Tue, 09 Jul 2013
* v0.21.5 - more documentation


#### Mon, 08 Jul 2013
* v0.21.5 - added more documentation


#### Thu, 04 Jul 2013
* v0.21.4 - ByteBufferResponseBody can now cope with bad Content-Length headers (e.g. from Apache1)


#### Thu, 04 Jul 2013
* v.021.3 Documentation and examples


#### Thu, 04 Jul 2013
* v0.21.3 InputStreamResponseBody now has try-catch in iterate method body to ensure stream is closed.


#### Thu, 04 Jul 2013
* v0.21.3 ByteBufferResponseBody.toBufferedBody is now a lazy val


#### Sat, 08 Jun 2013
* v0.21.2
* Endpoint now supports a userinfo field and the corresponding authority segment.
* Href is able to handle file urls now.
* Some broken tests fixed.


#### Sat, 11 May 2013
* v0.21.1
* ResponseBody instances now all have a close() method to support clean use of the unbuffered implementation, although this is never needed in the buffered case.
* When making an unbuffered request, an unbuffered response is only returned in the 200 OK case; in all other cases the response is buffered, ensuring the input stream is cleanly closed in error flows.
* Now supports using slf4j v1.7.x as well as earlier versions
* Updates mime types


#### Fri, 19 Apr 2013
* Scripts and metadata changed.


#### Thu, 18 Apr 2013
* v0.21.0
* Bug fixed: HttpClient: when redirecting, the socket input stream was not always closed cleanly.
* Bug fixed: HttpUtil.copyToByteBufferAndClose now wraps input stream close in a finally block.


#### Thu, 18 Apr 2013
* v0.20.5
* SelfClosingInputStreamDelegate used to ensure that unbuffered response bodies can still be sure that their source input stream will always be closed, provided they consume all the source data.


#### Wed, 27 Mar 2013
* Upsync scripting improvements


#### Mon, 25 Mar 2013
* Maven support script improved.


#### Fri, 15 Mar 2013
* More build script work done.


#### Fri, 15 Mar 2013
* Changes to the build scripts only.


#### Fri, 15 Mar 2013
* Now supports Scala 2.9.3 and 2.10.1


#### Mon, 18 Feb 2013
* v0.20.4


#### Mon, 18 Feb 2013
* v0.20.4
* Bug fix: NPE in InputStreamDelegate.
* HttpClient documentation.


#### Fri, 15 Feb 2013
* v0.20.3


#### Thu, 14 Feb 2013
* v0.20.3
* Tweaks made to ResposneBody and its implementations..


#### Thu, 14 Feb 2013
* v0.20.2
* Bug fixed: when the streamed response body is used, the URL connection 'disconnect()' would immediately close the stream before it could be used. This is now fixed: the input stream is wrapped in a decorator that automatically disconnects the connection after the stream is closed.


#### Thu, 14 Feb 2013
* v0.20.1
* InputStreamResponseBody now provides an iterator correctly..


#### Wed, 13 Feb 2013
* v0.20.0
* Rework of ResponseBody - it's now possible to get access to the InputStream, possibly filtered line by line.


#### Wed, 13 Feb 2013
* v0.19.1
* More minor authentication work done.


#### Thu, 07 Feb 2013
* v0.19.1
* Some more work done on digest authentication, particularly testing.


#### Thu, 07 Feb 2013
* v0.19.0


#### Thu, 07 Feb 2013
* v1.0.1


#### Thu, 07 Feb 2013
* v1.0.1
* Now supports Scala 2.10 and uses Sbt 0.12.2


#### Thu, 07 Feb 2013
* v0.19.0
* Support for Scala 2.10


#### Fri, 11 Jan 2013
* ResponseBody is now an Iterable.


#### Thu, 10 Jan 2013
* v0.18.12
* ResponseBody has new iterator method that returns the lines of the body via an iterator
* sbt now uses v0.12.1


#### Thu, 10 Jan 2013
* v0.18.11
* StringRequestBody extra flush`


#### Thu, 10 Jan 2013
* v0.18.11
* The RequestBody implementation classes have been changed considerably.
* HttpServletRequestAdapter and HttpServletResponseAdapter have been rewritten.
* new TextualBodyFilter


#### Wed, 09 Jan 2013
* v0.18.11
* new Splitter for low-memory extraction of substrings in a string.


#### Wed, 09 Jan 2013
* v0.18.11
* PartialURL is replaced by an identical class called Href. PartialURL is still available but is now deprecated and will be removed later.
* CookieJar is now a Seq.
* New header value classes CommaListValue and SemicolonListValue.
* MediaType now treats application/javascript and application/ecmascript as textual types.
* some more tidying up


#### Wed, 09 Jan 2013
* HttpUtilTest tests captureBytes now


#### Tue, 08 Jan 2013
* v0.18.10
* Some simplification to RequestBody
* new StringSeqRequestBody
* HttpServletRequestAdapter now has filtering capability like HttpServletResponseAdapter


#### Thu, 20 Dec 2012
* v0.18.9
* Header classes clarify the usage of ignore-case comparison.
* Domain tweaked slightly.


#### Thu, 20 Dec 2012
* Some tidying up.
* Domain moved to url package.
* Endpoint is in its own file.


#### Thu, 20 Dec 2012
* Some tidying up.
* Domain moved to url package.
* Endpoint is in its own file.


#### Thu, 20 Dec 2012
* Bug fixed: HttpClient propagates exceptions correctly now.
* RequestBody rewritten as a trait and implementing classes.


#### Thu, 20 Dec 2012
* RequestBody rewritten to allow for caching when processing digest authentication


#### Wed, 19 Dec 2012
* v0.18.8
* - small change in dependencies
* - deflate algorithm tried out but not yet working (only gzip is supported)


#### Fri, 14 Dec 2012
* v0.18.7
* commonRequestHeaders restored - now provided via Config


#### Fri, 14 Dec 2012
* v0.18.6
* access logging improved - use logback.xml to configure the level you want


#### Fri, 14 Dec 2012
* v0.18.6
* More work done to support digest authentication. First live test has passed!


#### Fri, 07 Dec 2012
* v0.18.5
* New AuthenticationRegistry provides an easily-testable framework to support automatic authentication in HttpBrowser.


#### Thu, 06 Dec 2012
* v0.18.5
* Further work done one automatic authentication in HttpBrowser.


#### Wed, 05 Dec 2012
* v0.18.4
* More work done on authentication. First working implementation in HttpBrowser of automatic basic authentication.


#### Tue, 04 Dec 2012
* v0.18.4
* Build scripts now handle maven repository format correctly.


#### Tue, 04 Dec 2012
* v0.18.3
* PreRequest handlers for hostnameVerifier and sslSocketFactory tweaked a little.


#### Mon, 03 Dec 2012
* v0.18.3
* Config now has two new options: hostnameVerifier and sslSocketFactory. Individual HttpClient instances can now be
*  set up with bespoke SSL settings to suit their intended remote access point.


#### Fri, 30 Nov 2012
* v0.18.1
* More testing. Added JSON parsing for httpbin.org integration tests.


#### Wed, 28 Nov 2012
* v0.18.1
* DigestCredential now computes the digest response as specified by RFC2617. More work is still needed to provide round-trip digest authentication support.


#### Wed, 28 Nov 2012
* v0.18.0
* Upgraded to support Maven / Ivy correctly
* Stopped using Slf4s because it limits supported Scala versions and is not essential.
* HttpDateTimeInstant now adds support for ISO8601.


#### Wed, 07 Nov 2012
* PartialURL and Request allow augmentation with query parameters
* Deprecated SplitURL has been deleted - use PartialURL now.
* More testing.


#### Wed, 07 Nov 2012
* 0.17.3
* New Http abstract base class is common to HttpClient and HttpBrowser.


#### Thu, 25 Oct 2012
* Added overloaded '+' methods for cookie jars when altering Request instances.


#### Mon, 22 Oct 2012
* v0.17.2
* Config: new SSLSocketFactoryInjecter and HostnameVerifierInjecter replace the parameters.


#### Mon, 22 Oct 2012
* v0.17.1
* More work done on DigestCredential - still incomplete


#### Mon, 22 Oct 2012
* v0.17.0
* Config changed to allow more extensibility.
* new PreRequest mechanism for inserting behaviour during the construction of an HttpURLConnection


#### Wed, 17 Oct 2012
* v0.16.2
* More work done on digest authentication. Still not finished.
* Dependencies now fixed to specific versions because compilation is soo much quicker.


#### Wed, 17 Oct 2012
* v0.16.1
* New AuthenticateValue handles WWW-Authenticate/Proxy-Authenticate headers
* Digest authentication is not yet implemented.


#### Mon, 15 Oct 2012
* (changelog)


#### Wed, 10 Oct 2012
* v0.16.0
* Borrowed a public-domain Base64 encoder (Java). New B64Util class.
* More work done on basic authentication
* Config now includes a CredentialSuite. Not yet used.


#### Tue, 09 Oct 2012
* v0.16.0
* HttpClient.execute renamed makeRequest (because of Scala not allowing default arguments on overloaded methods)
* new Endpoint class augments PartialURL
* HttpServletRequestAdapter reverted to v0.13 version


#### Mon, 08 Oct 2012
* v0.15.1
* Lots of extra test resources for testing authentication and https.
* Basic-auth password file is user bigbee password HelloWorld.


#### Thu, 04 Oct 2012
* v0.15.0


#### Thu, 04 Oct 2012
* Now fully renamed to bee-client.


#### Thu, 04 Oct 2012
* v0.14.2
* HttpIntegration - some new tests for HTTPS


#### Wed, 03 Oct 2012
* v0.14.2
* Cookie now represents maxAge correctly.
* Cookie now supports conversion to javax servlet Cookie
* Gleaning cookies from the response now involves the set-cookie headers being removed from the remaining response headers.
* Progress with URLMapper and HttpServletRequestAdapter.


#### Wed, 03 Oct 2012
* v0.14.0
* Path corrections - now more assurance it works correctly.
* Tests revised.


#### Tue, 02 Oct 2012
* v0.14.0
* Path corrections - now more assurance it works correctly.


#### Tue, 02 Oct 2012
* v0.14.0
* New URLMapper
* New PartialURL & Path


#### Tue, 02 Oct 2012
* v0.13.5
* HttpServletResponseAdapter.setResponseHeaders tweaked..


#### Tue, 02 Oct 2012
* v0.13.5
* HttpServletResponseAdapter converts location headers in the response using the same mapping function that is applied to the response body.
* HeaderName traits now individually exposed via companion objects.


#### Thu, 27 Sep 2012
* v0.13.4
* Documentation changes.


#### Tue, 18 Sep 2012
* v0.13.4
* New script for generating the changelog.


#### Tue, 18 Sep 2012
* v0.13.4
* HttpServletResponseAdapter has a new setResponseHeaders method


#### Tue, 18 Sep 2012
* v0.13.3
* DST TimeZone bug in HttpDateTimeInstant has been fixed.
* New fast parsing algorithm for RFC1123 dates (about 4x faster than SimpleDateFormat).


#### Tue, 18 Sep 2012
* v0.13.2
* New HttpBrowser utility exists now.


#### Mon, 17 Sep 2012
* v0.13.1
* CookieJar is now an Iterable[CookieIdentity]


#### Mon, 17 Sep 2012
* v0.13.0
* Considerable improvement in support for redirection. Cookies are now preserved over redirections (unlike using HttpURLConnection)


#### Mon, 17 Sep 2012
* v0.12.3
* Status now has a single-argument apply method for convenience.


#### Sun, 16 Sep 2012
* v0.12.2
* Cookie.expires is now optional, as per the Set-cookie behaviour.
* Cookie.lastAccessed was not used and has been dropped.
* Domain is no longer a case class so that the object apply methods can be specified fully.
* More testing.


#### Sat, 15 Sep 2012
* v0.12.1
* QualifiedValue rationalised. Qualifier renamed as NameVal. QualifiedPart renamed as Qualifiers.


#### Sat, 15 Sep 2012
* v0.12.0
* Cookie API simplified. Former CookieValue class no longer exists and the Cookie class provides its function directly. A new CookieIdentity trait is implemented by Cookie and CookieKey, allowing them to be matched correctly.


#### Fri, 14 Sep 2012
* v0.11.3
* Date parsing now accommodates another non-standard format.


#### Fri, 14 Sep 2012
* v0.11.2
* BufferedResponseBuilder now only provides a request URL when the request was a GET and the status was success.


#### Fri, 14 Sep 2012
* v0.11.2
* Refinement to the algorithm for guessing media type now allows for unlabelled text and html.


#### Thu, 13 Sep 2012
* v0.11.1
* ByteBufferResponseBody is now able to use the file extension when guessing the media type.
* New MimeTypes wrapper.
* SplitURL has new file & extension methods


#### Thu, 13 Sep 2012
* v0.11.0
* More test code- new ResponseBuilderTest


#### Thu, 13 Sep 2012
* v0.11.0
* ByteBufferResponseBody now has an algorithm to guess a sensible media type in the (rare) cases when they are missing from the response. This allows basic servers to be handled sensibly.


#### Wed, 12 Sep 2012
* v0.10.3
* Improved code documentation.


#### Tue, 11 Sep 2012
* v0.10.2
* upsync bug fixed


#### Mon, 10 Sep 2012
* v0.10.2
* Header parsing now has validation checking.


#### Mon, 10 Sep 2012
* v0.10.2
* HeaderNames divided by role.
* Status now has isBodyAllowed


#### Sun, 09 Sep 2012
* v0.10.1
* Dependency correction.


#### Sun, 09 Sep 2012
* v0.10.1
* Comments, documentation and examples


#### Fri, 07 Sep 2012
* v0.10.0
* Response now includes the returned cookies, as an option.
* HttpClient 'easy' methods now come in two variants, one with cookies and one without.
* HttpClient now gleans cookie changes automatically.


#### Fri, 07 Sep 2012
* v0.9.9
* Request enhanced to include the its request headers and the cookies available to it.


#### Fri, 07 Sep 2012
* v0.9.8
* Bug fixed: Response apply method now returns a value correctly
* Status codes revised to be more useful.


#### Fri, 07 Sep 2012
* v0.9.7
* CookieJar duplicate code removed; new 'find' method added.


#### Thu, 06 Sep 2012
* v0.9.7
* More documentation


#### Thu, 06 Sep 2012
* v0.9.7
* Cookie apply method: no default value is provided for the domain now
* Domain: localhost and hostname are now separate
* Headers: new set method makes clear the difference between setting and adding headers.
* Simplified php test scripts; simplified examples.
* New cookie example


#### Thu, 06 Sep 2012
* v0.9.6
* upsync script tweaked


#### Thu, 06 Sep 2012
* v0.9.6
* upsync script tweaked


#### Thu, 06 Sep 2012
* v0.9.6
* Headers api tweaked.


#### Wed, 05 Sep 2012
* v0.9.5
* RequestBody should take the data before the mediatype parameter - the parameter order has been swapped to allow for defaults.
* Examples tweaked.


#### Wed, 05 Sep 2012
* v0.9.4
* improved implicit string-to-url conversion
* simplified examples
* new apply constructor for Response


#### Tue, 04 Sep 2012
* v0.9.3
* new ResponseBody.isTextual method
* ByteBufferResponseBody.asString now always returns an empty string if the content is binary


#### Tue, 04 Sep 2012
* v0.9.2
* Default headers now include "Accept: */*" and the accept_charset allows *;q=.1
* More testing.


#### Mon, 03 Sep 2012
* v0.9.1
* More test and example code.


#### Mon, 03 Sep 2012
* v0.9.0
* Config moved to top-level package, alongside HttpClient.
* Config now includes keepAlive, userAgentString amd proxy settings.
* HttpClient proxy parameter has been removed - now part of Config.
* HttpClient now includes config request headers in the protocol exchange.
* New DNT (do not track) header added to HeaderName.
* Headers has slightly enhanced api.
* RequestBody now always flushes output streams when done.
* SplitURL has better support for query strings.
* New HttpGlobalSettings added for non-api based global settings.


#### Thu, 02 Aug 2012
* Docs


#### Thu, 02 Aug 2012
* Docs


#### Wed, 01 Aug 2012
* v0.8.5
* Minor script change.


#### Wed, 01 Aug 2012
* v0.8.5
* HttpClient now has the implicit conversion method.
* Some dead items removed.
* New examples added.


#### Tue, 31 Jul 2012
* v0.8.4
* Plans


#### Tue, 31 Jul 2012
* v0.8.4
* Dead code removed. Unused dependencies removed.


#### Wed, 18 Jul 2012
* v0.8.3
* ResponseBody now has a contentLength method.
* Headers api is a bit more Scala-collection like.


#### Wed, 18 Jul 2012
* v0.8.2
* new Cookie object with apply method for easier construction of cookies


#### Mon, 09 Jul 2012
* v0.8.1
* HttpClient.execute is overloaded with one version with an internal response builder, and the other with a supplied response builder.


#### Mon, 09 Jul 2012
* v0.8.0
* Request.post has a body that is now optional


#### Fri, 06 Jul 2012
* v0.7.10
* ByteBufferResponseBody bug fixed - empty content returns an empty string, whether binary or not.


#### Thu, 05 Jul 2012
* v0.7.10
* CookieJar.cookies is now a List


#### Thu, 05 Jul 2012
* v0.7.9
* CookieValue now contains a string field (previously called value)


#### Thu, 05 Jul 2012
* v0.7.9
* cookie jars now differentiate between map and list storage
* new filter method in CookieJar


#### Wed, 04 Jul 2012
* v0.7.8
* extraction of cookies from responses has been tidied up a bit


#### Fri, 15 Jun 2012
* v0.7.7
* revised dependencies
* SplitURL now has path method
* work on HttpServletResponseAdapter
* NPE fixed in HttpUtil.copyText


#### Thu, 14 Jun 2012
* v0.7.6 - @throws added to public methods so that Java can catch exceptions


#### Thu, 07 Jun 2012
* v0.7.5: Util renamed HttpUtil in new sub-package and is no longer private.


#### Tue, 05 Jun 2012
* v0.7.5: Util renamed HttpUtil in new sub-package and is no longer private.


#### Tue, 22 May 2012
* v0.7.4 - Bugfix - unnecessary restriction on POST/PUT bodies removed.


#### Mon, 21 May 2012
* v0.7.3 - Potential null in response parser is now handled gracefully - another case.


#### Mon, 21 May 2012
* v0.7.2 - Potential null in response parser is now handled gracefully.


#### Fri, 27 Apr 2012
* v0.7.1 - Some faults fixed within test code.


#### Thu, 26 Apr 2012
* v0.7.0 - MediaType `type` is now contentType because of compiler bugs.


#### Thu, 26 Apr 2012
* v0.6.4 - Default constructor arg re-added to CookieJar.


#### Thu, 26 Apr 2012
* v0.6.4 - Domain now determines the name of localhost for use as the default when creating cookie keys.


#### Thu, 26 Apr 2012
* v0.6.3 - CookieJar tweaked


#### Thu, 26 Apr 2012
* v0.6.3 - new alteration methds added to CookieJar


#### Wed, 25 Apr 2012
* v0.6.2: BufferedResponseFactory is no longer private.


#### Tue, 24 Apr 2012
* 0.6.1


#### Wed, 18 Apr 2012
* v0.6.0: HttpClient.execute does not repackage IOExceptions, they simply bubble up. RequestException was deleted.


#### Tue, 17 Apr 2012
* v0.5.0: HttpClient.execute does not throw an exception for 4xx and 5xx status codes now. It only throws an exception if an IOException is caught.


#### Fri, 30 Mar 2012
* v0.4.0: MediaType may be unknown on certain requests - eg.g POST with 204 response. Therefore it is now optional in the response.


#### Thu, 29 Mar 2012
* Problem with missing media type being investigated. Added logging at debug level to aid diagnosis.


#### Wed, 28 Mar 2012
* v0.3.6: Further integration testing. Now tested against Apache2, Nginx, Lighttpd and Cherokee.


#### Wed, 28 Mar 2012
* v0.3.6. MediaType has new isTextual method. HttpServletResponseAdapter allows conditional rewriting of textual content.


#### Wed, 28 Mar 2012
* v0.3.5. Headers improved. SplitURL is now a useful tool. HttpServletRequestAdapter works for simple requests now.


#### Wed, 28 Mar 2012
* v0.3.4. SplitURL implicit converters.


#### Wed, 28 Mar 2012
* v0.3.4. New SplitURL utility.


#### Tue, 27 Mar 2012
* v0.3.3: ResponseBody and ByteBufferResponseBody simplified. HttpServletResponseAdapter replaces CopyStreamResponseBody, which has been deleted. More HttpServletRequestAdapter testing.


#### Tue, 27 Mar 2012
* v0.3.2: HttpServletAdapters implemented but not yet tested.


#### Tue, 27 Mar 2012
* v0.3.1. ResponseBodyFactory replaced with ResponseFactory, which is able to handle responses in arbitrary ways. Request simplified again.


#### Tue, 27 Mar 2012
* v0.3.0. Reworking of request and response bodies - they now have different names.
* Request now has a ResponseBodyFactory parameter.
* HttpClient does not have a fixed ResponseBodyFactory but uses the one in the request.


#### Thu, 22 Mar 2012
* More work on the shell support classes.


#### Wed, 21 Mar 2012
* New trial of shell DSL.


#### Wed, 14 Mar 2012
* Comments.


#### Thu, 08 Mar 2012
* v0.2.3: More work done on headers of various types


#### Tue, 06 Mar 2012
* v0.2.2: bugfix - options was not passing its request entity


#### Tue, 06 Mar 2012
* v0.2.1: further test code


#### Tue, 06 Mar 2012
* v0.2.1: proxy support added; further test code


#### Tue, 28 Feb 2012
* v0.2.0
* Switched to use ScalaTest; JUnit was removed.


#### Mon, 27 Feb 2012
* Change of package name: lhc -> http


#### Mon, 27 Feb 2012
* v0.1.8
* Config bug-fix: default read timeout is now 5sec.
* Body toString implemented to minimise potential surprise.


#### Fri, 24 Feb 2012
* v0.1.8
* Documentation improved a little.


#### Wed, 22 Feb 2012
* v0.1.7
* CookieJar is now working.


#### Tue, 21 Feb 2012
* v0.1.6
* Some reworking of response body with fewer classes/traits, so as to reduce the likelihood of needing casts.


#### Tue, 21 Feb 2012
* Domain now in a separate file.
* CookieJar.filterForRequest now implemented.


#### Tue, 21 Feb 2012
* HttpDateTime renamed HttpDateTimeInstant


#### Tue, 21 Feb 2012
* CookieJar now supports deletion of expired cookies.
* Cookie2 header is marked obsolete as per RFC6265/IANA.


#### Mon, 20 Feb 2012
* Some minor corrections.
* Version now v0.1.5


#### Mon, 20 Feb 2012
* HttpDateTime now replaces HttpDate


#### Mon, 20 Feb 2012
* CookieJar now adding cookies correctly.


#### Mon, 20 Feb 2012
* Lots of work done on cookies.
* New StringBodyCache.
* HttpDate now separated out.


#### Tue, 14 Feb 2012
* v0.1.4
* Response includes its request.
* Config no longer supports keepAlive or chunkSize for the time being.
* A bit of tidying up.


#### Mon, 13 Feb 2012
* Config chunk size added - but not yet working.
* Some bugs fixed.


#### Mon, 13 Feb 2012
* Implicit conversions used for Headers as List[Header] and HeaderName as String


#### Mon, 13 Feb 2012
* Config and RequestException are now in separate files.
* Config object deleted.


#### Mon, 13 Feb 2012
* CleanupThread now in a separate file.
* Keep-alive flag moved into Config class.


#### Sun, 12 Feb 2012
* CleanupThread stays as a singleton.


#### Sat, 11 Feb 2012
* More tidying up.
* New Headers class wraps up request and response header lists.


#### Fri, 10 Feb 2012
* Renaming and refactoring.
* More tests.


#### Fri, 10 Feb 2012
* Now using JavaStubServer 0.4
* Re-organised header/request/response packages
* Header substructure will now be handled using additional case classes instead of being an inheritance tree.


#### Tue, 07 Feb 2012
* New ResponseBodyFactory now provides pluggable creation of the response objects.


#### Tue, 07 Feb 2012
* ResponseBody is now used by Response and HttpClient. This allows for a choice between streamed extraction and buffered extraction of the response data.


#### Mon, 06 Feb 2012
* Host header handling simplified to be configured via a flag in RequestConfig.


#### Mon, 06 Feb 2012
* v0.1.2


#### Mon, 06 Feb 2012
* Better handling of response errors.
* New ResponseBody allows buffered or streamed modes of operation. Not yet integrated.


#### Mon, 06 Feb 2012
* More test data - a photograph from Greece, July 2011 (Rick Beton)


#### Mon, 06 Feb 2012
* Further testing & minor bugs fixed.


#### Mon, 06 Feb 2012
* Body renamed to RequestBody and moved to its own source file.
* RequestBody is able to accept streamed data if required, although direct copying from strings or maps is also supported (as previously).


#### Mon, 06 Feb 2012
* New setup script for the webserver testing.


#### Mon, 06 Feb 2012
* Minor changes to test code.


#### Mon, 30 Jan 2012
* RequestConfig is now a case class.
* More integration testing.


#### Mon, 30 Jan 2012
* Minor simplification in Header.


#### Wed, 25 Jan 2012
* Improvements to header handling.
* Dates are now treated as per RFC2616.


#### Mon, 23 Jan 2012
* Minor dependency tweaks.


#### Mon, 23 Jan 2012
* New Maven2 pom.


#### Mon, 23 Jan 2012
* Some tidying up.
* Logback re-introduced for testing (only), because Jetty uses logging.


#### Mon, 23 Jan 2012
* Integration test now runs via JUnit but skips the tests if the test webserver is not reachable.


#### Mon, 23 Jan 2012
* Dependencies sharply reduced, to prepare for pom bundling.


#### Fri, 20 Jan 2012
* More integration testing done. Bugs fixed as discovered.


#### Tue, 17 Jan 2012
* Http class renamed HttpClient.
* More tests.


#### Tue, 17 Jan 2012
* Some refactoring.


#### Tue, 17 Jan 2012
* Complete set of HTTP methods is now supported.
* Clarification made of which methods require entity bodies.


#### Tue, 17 Jan 2012
* Lots of development & testing.
* New cleanup thread for eventually disconnecting keep-alive connections


#### Mon, 16 Jan 2012
* Added tests for PUT, POST, DELETE


#### Mon, 16 Jan 2012
* Http now allows lazy closing of connections.


#### Mon, 16 Jan 2012
* New HttpTest.
* New stubserver dependency.


#### Fri, 13 Jan 2012
* First cut at the new API


