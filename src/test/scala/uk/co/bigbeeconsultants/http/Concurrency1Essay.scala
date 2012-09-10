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

package uk.co.bigbeeconsultants.http

import header.MediaType
import java.net.{ConnectException, Proxy, URL}
import uk.co.bigbeeconsultants.http.HttpIntegration._

object Concurrency1Essay {
  //  HttpGlobalSettings.maxConnections = 10
  val nIterations = 100000
  val nThreads = 1
  val nIterationsEach = nIterations / nThreads
//  val url = testPhotoUrl
  val url = serverUrl + testTxtFile

  //  val proxyAddress = new InetSocketAddress("localhost", 8888)
  //  val proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
  val proxy = Proxy.NO_PROXY

  val config = Config(followRedirects = false, proxy = proxy, keepAlive = true, userAgentString = Some("Wahey!"))

  def main(args: scala.Array[scala.Predef.String]) {
    require(nThreads > 0)
    val threads = for (i <- 1 to nThreads) yield new Concurrency1Thread(i)
    val start = System.currentTimeMillis()
    for (t <- threads) t.start()
    for (t <- threads) t.join()
    val end = System.currentTimeMillis()
    println((end - start) + "ms")
  }

  val http = new HttpClient(config)
}

class Concurrency1Thread(id: Int) extends Thread {

  import HttpIntegration._
  import Concurrency1Essay._

  override def run() {
    require(nIterationsEach > 0)
    val u = url + "?t" + id
    try {
      for (i <- 1 to nIterationsEach) {
        htmlGet(u + "&i=" + i)
      }
    } catch {
      case e: Exception =>
        skipTestWarning("GET", u, e)
    }
  }

  private def htmlGet(url: String) {
    val response = http.get(new URL(url), gzipHeaders)
    assert(response.status.code == 200, url + " " + response.status.code.toString)
    assert(response.body.contentType == MediaType.TEXT_PLAIN, response.body.contentType)
    val bytes = response.body.toString
    assert(bytes.length == testTxtSize, bytes.length)
    //assert(response.body.contentType == MediaType.IMAGE_JPG, response.body.contentType)
    //val bytes = response.body.asBytes
    //assert(bytes.length == testPhotoSize, bytes.length)
  }

  private def skipTestWarning(method: String, url: String, e: Exception) {
    if (e.isInstanceOf[ConnectException] || e.getCause.isInstanceOf[ConnectException]) {
      System.err.println("***** Test skipped: " + method + " " + url + " : " + e.getMessage)
    }
    else {
      throw e
    }
  }
}