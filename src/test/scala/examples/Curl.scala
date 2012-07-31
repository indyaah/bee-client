package examples

import scala.Predef._
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.request.Config
import java.net.URL

object Curl extends App {

  def curl(console: (String) => Unit, url: String) {
    val httpClient = new HttpClient(Config(followRedirects = false))
    val response = httpClient.get(new URL(url))
    println(response.status)
    println(response.body.asString)
  }

  var console = (message: String) => {}
  var i = 0
  while (i < args.length) {
    args(i) match {
      case "-v" =>
        console = (message: String) => println(message)
      case url =>
        curl(console, url)
    }
    i += 1
  }
}
