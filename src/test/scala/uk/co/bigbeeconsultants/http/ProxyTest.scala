package uk.co.bigbeeconsultants.http

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import co.freeside.betamax.{TapeMode, Recorder}
import java.io.File
import co.freeside.betamax.proxy.jetty.ProxyServer
import uk.co.bigbeeconsultants.http.util.DumbTrustManager
import java.net.{UnknownHostException, InetSocketAddress, URL, Proxy}

class ProxyTest extends FunSuite with ShouldMatchers with BeforeAndAfterAll {

  var recorder: Recorder = null
  var proxyServer: ProxyServer  = null

  override def beforeAll() = {
    recorder = new Recorder()
    recorder.setSslSupport(true)

    recorder.setTapeRoot(new File("src/test/resources/betamaxTapes"))
    recorder.setDefaultMode(TapeMode.READ_ONLY)
    recorder.insertTape("testTape")

    proxyServer = new ProxyServer(recorder)
    proxyServer.start()
  }

  override def afterAll() = {
    recorder.ejectTape()
    proxyServer.stop()
  }


  test("Should work with a proxy without being configured") {
    val conf = Config(
      sslSocketFactory = Some(DumbTrustManager.sslSocketFactory),
      hostnameVerifier = Some(DumbTrustManager.hostnameVerifier)
    )

    val client = new HttpClient(conf)
    val url = new URL("https://www.example.nope/test")
    val response = client.get(url)

    response.status.code should equal(200)
    response.body.asString should equal("Hey look some text")
  }

  test("Should work with a proxy being explicity configured") {
    val proxyAddress = new InetSocketAddress("localhost", 5555)

    val conf = Config(
      sslSocketFactory = Some(DumbTrustManager.sslSocketFactory),
      hostnameVerifier = Some(DumbTrustManager.hostnameVerifier),
      proxy = Some(new Proxy(Proxy.Type.HTTP, proxyAddress))
    )

    val client = new HttpClient(conf)
    val url = new URL("https://www.example.nope/test")
    val response = client.get(url)

    response.status.code should equal(200)
    response.body.asString should equal("Hey look some text")
  }

  test("Should fail properly explicitly setting no proxy") {
    val conf = Config(
      sslSocketFactory = Some(DumbTrustManager.sslSocketFactory),
      hostnameVerifier = Some(DumbTrustManager.hostnameVerifier),
      proxy = Some(Proxy.NO_PROXY)
    )

    val client = new HttpClient(conf)
    val url = new URL("http://www.example.nope/test")

    intercept[UnknownHostException] {
      client.get(url)
    }

  }

}
