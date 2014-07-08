package uk.co.bigbeeconsultants.http

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.net.{UnknownHostException, InetSocketAddress, Proxy}
import com.pyruby.stubserver.{StubMethod, StubServer}
import uk.co.bigbeeconsultants.http.header.MediaType._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProxyTest extends FunSuite with ShouldMatchers with BeforeAndAfter {

  val expectedBody = "Hey look some text"

  val vanillaConfig = Config(followRedirects = false).allowInsecureSSL

  test("Http should work with a proxy without being configured") {
    System.setProperty("http.proxyHost", "localhost")
    System.setProperty("http.proxyPort", port.toString)
    try {
      val conf = vanillaConfig.copy(proxy = None)
      val client = new HttpClient(conf)
      val url = "http://www.example.nope/test"
      val stubbedMethod = StubMethod.get("/test")
      server.expect(stubbedMethod).thenReturn(200, TEXT_PLAIN, expectedBody)

      val response = client.get(url)

      server.verify()
      response.status.code should equal(200)
      response.body.asString should equal(expectedBody)
    } finally {
      System.clearProperty("http.proxyHost")
      System.clearProperty("http.proxyPort")
    }
  }

  test("Http should work with a proxy being explicitly configured") {
    val proxyAddress = new InetSocketAddress("localhost", port)
    val conf = vanillaConfig.copy(proxy = Some(new Proxy(Proxy.Type.HTTP, proxyAddress)))
    val client = new HttpClient(conf)
    val url = "http://www.example.nope/test"
    val stubbedMethod = StubMethod.get("/test")
    server.expect(stubbedMethod).thenReturn(200, TEXT_PLAIN, expectedBody)

    val response = client.get(url)

    server.verify()
    response.status.code should equal(200)
    response.body.asString should equal("Hey look some text")
  }

  test("Http should fail properly when explicitly setting no proxy") {
    val conf = vanillaConfig.copy(proxy = Some(Proxy.NO_PROXY))

    val client = new HttpClient(conf)
    val url = "http://www.example.nope/test"

    intercept[UnknownHostException] {
      client.get(url)
    }
  }


  before {
    server = new StubServer()
    server.start()
    port = server.getLocalPort
  }

  after {
    server.clearExpectations()
    server.stop()
  }

  private var port = 0
  private var server: StubServer = null
}
